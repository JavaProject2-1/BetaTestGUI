package basicWeb;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

/**
 * CrawlerExample 방식을 사용한 상세 과목 크롤러
 */
public class DetailedSubjectCrawler {

    /**
     * 셀 내용을 텍스트로 추출
     */
    public static String getCellText(WebElement cell) {
        try {
            List<WebElement> nobrList = cell.findElements(By.tagName("nobr"));
            if (!nobrList.isEmpty()) {
                String innerHtml = nobrList.get(0).getDomProperty("innerHTML");
                String textWithNewlines = innerHtml.replaceAll("(?i)<br[^>]*>", "\n");
                return textWithNewlines.replaceAll("<[^>]+>", "").trim();
            }
            return cell.getText().trim();
        } catch (NoSuchElementException e) {
            return cell.getText().trim();
        }
    }

    /**
     * 강의 고유 키 생성 (중복 제거용)
     */
    private static String getCourseKey(List<WebElement> cells) {
        String code = getCellText(cells.get(7));
        String name = getCellText(cells.get(8));
        String professor = getCellText(cells.get(12));
        String time = getCellText(cells.get(13));
        return code + "|" + name + "|" + professor + "|" + time;
    }

    /**
     * 크롬 드라이버 설정 (운영체제별 자동 감지)
     */
    private static void setupChromeDriver() {
        String os = System.getProperty("os.name").toLowerCase();
        String driverPath;

        // 운영체제별 드라이버 경로 설정
        if (os.contains("win")) driverPath = "chromedriver.exe";
        else if (os.contains("mac")) driverPath = "chromedriver_mac";
        else if (os.contains("nux") || os.contains("nix")) driverPath = "chromedriver_linux";
        else throw new RuntimeException("지원하지 않는 운영체제입니다: " + os);

        // 절대 경로로 변환
        String absolutePath = Paths.get(driverPath).toAbsolutePath().toString();
        File driverFile = new File(absolutePath);

        // 파일 존재 여부 확인
        if (!driverFile.exists()) {
            throw new RuntimeException("크롬 드라이버 파일을 찾을 수 없습니다: " + absolutePath);
        }

        // Unix 계열 시스템에서 실행 권한 설정
        if (!os.contains("win")) {
            if (!driverFile.canExecute()) {
                driverFile.setExecutable(true);
            }
        }

        // 시스템 속성 설정
        System.setProperty("webdriver.chrome.driver", absolutePath);
    }

    /**
     * 과목명으로 상세 강의 정보 검색 (CrawlerExample 방식)
     */
    public static List<DetailedSubject> searchDetailedSubjects(String inputYearFull, String inputSemester, String subjectName) {
        setupChromeDriver();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);

        List<DetailedSubject> detailedSubjects = new ArrayList<>();
        Set<String> uniqueCourses = new HashSet<>();

        try {
            // 강의 계획서 페이지로 이동
            driver.get("https://knuin.knu.ac.kr/public/stddm/lectPlnInqr.knu");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            System.out.println("과목명으로 검색 중: " + subjectName);

            // 개설년도 입력
            WebElement yearInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schEstblYear___input")));
            js.executeScript("arguments[0].value=arguments[1];", yearInput, inputYearFull);

            // 학기 선택
            WebElement semesterSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[title='개설학기']")));
            new Select(semesterSelect).selectByVisibleText(inputSemester);

            // 학과 코드 초기화
            js.executeScript("document.getElementById('schSbjetCd1').value = '';");

            // 검색 조건을 교과목명으로 설정
            WebElement detailSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("schCode")));
            new Select(detailSelect).selectByVisibleText("교과목명");

            // 교과목명 입력
            WebElement inputBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schCodeContents")));
            inputBox.clear();
            inputBox.sendKeys(subjectName);

            // 검색 실행
            WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnSearch")));
            searchBtn.click();

            // 결과 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody[@id='grid01_body_tbody']/tr[1]")));
            Thread.sleep(1000);

            // 스크롤 가능한 영역에서 데이터 추출 (CrawlerExample 방식)
            WebElement scrollDiv = driver.findElement(By.id("grid01_scrollY_div"));
            Number scrollHeightNum = (Number) js.executeScript("return arguments[0].scrollHeight;", scrollDiv);
            double scrollHeight = scrollHeightNum.doubleValue();

            Number clientHeightNum = (Number) js.executeScript("return arguments[0].clientHeight;", scrollDiv);
            double clientHeight = clientHeightNum.doubleValue();

            double scrollTop = 0;
            double increment = 150;
            boolean newCourseFound;

            System.out.println("스크롤 영역 높이: " + scrollHeight + ", 클라이언트 높이: " + clientHeight);

            do {
                js.executeScript("arguments[0].scrollTop = arguments[1];", scrollDiv, scrollTop);
                Thread.sleep(800);

                List<WebElement> rows = driver.findElements(By.xpath("//tbody[@id='grid01_body_tbody']/tr"));
                newCourseFound = false;

                System.out.println("현재 스크롤 위치: " + scrollTop + ", 발견된 행 수: " + rows.size());

                for (int i = 0; i < rows.size(); i++) {
                    WebElement row = rows.get(i);
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.size() < 17) continue;

                    String key = getCourseKey(cells);
                    if (!uniqueCourses.contains(key)) {
                        uniqueCourses.add(key);

                        try {
                            String year = getCellText(cells.get(3));           // 학년
                            String code = getCellText(cells.get(7));           // 강좌번호
                            String name = getCellText(cells.get(8));           // 교과목명
                            String credit = getCellText(cells.get(9));         // 학점
                            String professor = getCellText(cells.get(12));     // 담당교수
                            String lectureTime = getCellText(cells.get(13));   // 강의시간
                            String classroom = getCellText(cells.get(15));     // 강의실
                            String roomNumber = getCellText(cells.get(16));    // 호실번호
                            String division = getCellText(cells.get(4));       // 교과구분

                            // 빈 데이터 필터링
                            if (name.isEmpty() || name.equals("-") || code.isEmpty() || code.equals("-")) {
                                continue;
                            }

                            // 필수/설계 여부 판단
                            boolean isRequired = division.contains("전공필수") || division.contains("교양필수");
                            boolean isDesign = division.contains("설계");

                            DetailedSubject detailedSubject = new DetailedSubject(
                                year, inputSemester, division,
                                code, name, isRequired, isDesign, credit,
                                professor, lectureTime, classroom, roomNumber
                            );

                            detailedSubjects.add(detailedSubject);
                            newCourseFound = true;

                            System.out.println("추가된 강의: " + name + " - " + professor + " (" + lectureTime + ")");

                        } catch (Exception e) {
                            System.err.println("행 파싱 중 오류: " + e.getMessage());
                            continue;
                        }
                    }
                }

                // 스크롤 종료 조건 확인
                if (scrollTop + clientHeight >= scrollHeight) break;
                scrollTop = Math.min(scrollTop + increment, scrollHeight - clientHeight);

            } while (newCourseFound || scrollTop + clientHeight < scrollHeight);

            System.out.println("총 " + detailedSubjects.size() + "개 강의 발견");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return detailedSubjects;
    }
}
