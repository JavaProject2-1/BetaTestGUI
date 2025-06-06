package basicWeb;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 과목명으로 검색하는 크롤러
 */
public class SubjectCrawler {
    
    /**
     * 과목명으로 강의 검색
     */
    public static List<DetailedSubjectWithGrade> searchSubjectsByName(String year, String semester, String subjectName) {
        setupChromeDriver();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);
        
        List<DetailedSubjectWithGrade> results = new ArrayList<>();
        
        try {
            driver.get("https://knuin.knu.ac.kr/public/stddm/lectPlnInqr.knu");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 개설연도 입력
            WebElement yearInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schEstblYear___input")));
            js.executeScript("arguments[0].value=arguments[1];", yearInput, year);

            // 학기 선택
            WebElement semesterSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[title='개설학기']")));
            new Select(semesterSelect).selectByVisibleText(semester);

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
            Thread.sleep(2000);
            
            try {
                WebElement tbody = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//tbody[@id='grid01_body_tbody']")));
                List<WebElement> rows = tbody.findElements(By.tagName("tr"));

                System.out.println("검색 결과: " + rows.size() + "개 행 발견");

                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.isEmpty() || cells.size() < 17) continue;

                    // 내용이 있는 행인지 확인
                    boolean hasContent = cells.stream().anyMatch(cell -> {
                        try {
                            String text = getCellText(cell);
                            return !text.isEmpty();
                        } catch (Exception e) {
                            return false;
                        }
                    });
                    
                    if (!hasContent) continue;

                    try {
                        // 각 셀에서 데이터 추출
                        String estblYear = getCellText(cells.get(1));    // 개설연도
                        String estblSemester = getCellText(cells.get(2)); // 개설학기
                        String grade = getCellText(cells.get(3));         // 학년
                        String division = getCellText(cells.get(4));      // 교과구분
                        String code = getCellText(cells.get(7));          // 강좌번호
                        String name = getCellText(cells.get(8));          // 교과목명
                        String credit = getCellText(cells.get(9));        // 학점
                        String professor = getCellText(cells.get(12));    // 담당교수
                        String lectureTime = getCellText(cells.get(13));  // 강의시간
                        String classroom = getCellText(cells.get(15));    // 강의실
                        String roomNumber = getCellText(cells.get(16));   // 호실번호

                        // 빈 데이터 필터링
                        if (name.isEmpty() || name.equals("-") || code.isEmpty() || code.equals("-")) {
                            continue;
                        }

                        // 필수/설계 여부 판단 (간단한 방식)
                        boolean isRequired = division.contains("전공필수") || division.contains("교양필수");
                        boolean isDesign = division.contains("설계");

                        DetailedSubjectWithGrade subject = new DetailedSubjectWithGrade(
                            estblYear, estblSemester, division, code, name, isRequired, isDesign, credit,
                            professor, lectureTime, classroom, roomNumber, grade
                        );

                        results.add(subject);
                        
                        System.out.println("추가된 과목: " + name + " (" + grade + "학년) - " + professor);

                    } catch (Exception e) {
                        System.err.println("행 파싱 중 오류: " + e.getMessage());
                        continue;
                    }
                }
                
            } catch (TimeoutException e) {
                System.out.println("검색 결과가 없거나 로딩 시간이 초과되었습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }

        System.out.println("총 " + results.size() + "개 과목 검색 완료");
        return results;
    }

    /**
     * 셀 텍스트 추출
     */
    private static String getCellText(WebElement cell) {
        try {
            List<WebElement> nobrList = cell.findElements(By.tagName("nobr"));
            if (!nobrList.isEmpty()) {
                String text = nobrList.get(0).getText();
                return text.replace("\n", " ").replaceAll("\\s+", " ").trim();
            }
            return cell.getText().replace("\n", " ").replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 크롬 드라이버 설정
     */
    private static void setupChromeDriver() {
        String os = System.getProperty("os.name").toLowerCase();
        String driverPath;

        if (os.contains("win")) driverPath = "chromedriver.exe";
        else if (os.contains("mac")) driverPath = "chromedriver_mac";
        else if (os.contains("nux") || os.contains("nix")) driverPath = "chromedriver_linux";
        else throw new RuntimeException("지원하지 않는 운영체제입니다: " + os);

        String absolutePath = Paths.get(driverPath).toAbsolutePath().toString();
        File driverFile = new File(absolutePath);

        if (!driverFile.exists()) {
            throw new RuntimeException("크롬 드라이버 파일을 찾을 수 없습니다: " + absolutePath);
        }

        if (!os.contains("win")) {
            if (!driverFile.canExecute()) {
                driverFile.setExecutable(true);
            }
        }

        System.setProperty("webdriver.chrome.driver", absolutePath);
    }
}