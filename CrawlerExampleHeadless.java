package Crawler;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

public class CrawlerExampleHeadless {

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

    private static Subject parseSubject(String year, String semester, String division,
                                        WebElement codeElem, WebElement nameElem, WebElement creditElem) {
        String code = codeElem.getText().trim();
        String html = nameElem.getAttribute("innerHTML");	
        String name = html.replaceAll("<[^>]*>", "").split("\\(")[0].trim();
        String credit = creditElem.getText().replaceAll("\\s+", "");

        if (code.isBlank() || code.equals("-") || credit.isBlank() || credit.equals("-")) return null;
        if (name.isBlank() || name.equals("-")) return null;

        boolean isRequired = html.contains("bum01");
        boolean isDesign = html.contains("bum02");

        return new Subject(year, semester, division, code, name, isRequired, isDesign, credit);
    }

    private static String getCourseKey(List<WebElement> cells) {
        String code = getCellText(cells.get(7));
        String name = getCellText(cells.get(8));
        String professor = getCellText(cells.get(12));
        String time = getCellText(cells.get(13));
        return code + "|" + name + "|" + professor + "|" + time;
    }

    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        String os = System.getProperty("os.name").toLowerCase();
        String driverPath;
        if (os.contains("win")) driverPath = "drivers/chromedriver.exe";
        else if (os.contains("mac")) driverPath = "drivers/chromedriver_mac";
        else if (os.contains("nux")) driverPath = "drivers/chromedriver_linux";
        else throw new RuntimeException("지원하지 않는 운영체제입니다.");

        try {
            System.out.println("몇년도에 입학하셨나요? ex)2021 or 2025");
            String userYear = scanner.nextLine().trim();

            String tabId = switch (userYear) {
                case "2020", "2021" -> "2021";
                case "2022" -> "2022";
                case "2023" -> "2023";
                case "2024", "2025" -> "2024";
                default -> "";
            };

            System.setProperty("webdriver.chrome.driver", driverPath);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-gpu");
            options.addArguments("--headless=new");
            WebDriver driver = new ChromeDriver(options);

            driver.get("https://cse.knu.ac.kr/sub3_2_b.php");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("tbody")));

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("document.querySelector('a[href=\"#' + arguments[0] + '\"]').click();", tabId);

            List<Subject> allSubjects = new ArrayList<>();
            String year = "", division = "";

            for (WebElement tbody : driver.findElements(By.tagName("tbody"))) {
                for (WebElement row : tbody.findElements(By.tagName("tr"))) {
                    List<WebElement> ths = row.findElements(By.tagName("th"));
                    List<WebElement> tds = row.findElements(By.tagName("td"));

                    if (!ths.isEmpty()) {
                        if (ths.size() == 2) {
                            year = ths.get(0).getText().trim();
                            division = ths.get(1).getText().trim();
                        } else if (ths.size() == 1) {
                            String txt = ths.get(0).getText().trim();
                            if (txt.matches("\\d")) year = txt;
                            else division = txt;
                        }
                    }
                    if (year.isEmpty()) continue;

                    if (tds.size() >= 3) {
                        Subject sub = parseSubject(year, "1학기", division, tds.get(0), tds.get(1), tds.get(2));
                        if (sub != null) allSubjects.add(sub);
                    }
                    if (tds.size() >= 6) {
                        Subject sub = parseSubject(year, "2학기", division, tds.get(3), tds.get(4), tds.get(5));
                        if (sub != null) allSubjects.add(sub);
                    }
                }
            }

            System.out.println("총 커리큘럼 과목 수: " + allSubjects.size());
            for (Subject s : allSubjects) System.out.println(s.getFormattedInfo());

            System.out.print("\n개설년도 입력 (예: 2025): ");
            String inputYearFull = scanner.nextLine().trim();

            System.out.print("학년 입력 (예: 1): ");
            String inputYear = scanner.nextLine().trim();

            System.out.print("학기 입력 (예: 1학기, 2학기, 계절학기(하계), 계절학기(동계)): ");
            String inputSemester = scanner.nextLine().trim();

            List<Subject> filtered = new ArrayList<>();
            for (Subject s : allSubjects) {
                if (s.year.equals(inputYear) && s.semester.equals(inputSemester)) filtered.add(s);
            }

            System.out.println("선택한 " + inputYear + "학년 " + inputSemester + " 과목 수: " + filtered.size());
            if (filtered.isEmpty()) return;

            driver.get("https://knuin.knu.ac.kr/public/stddm/lectPlnInqr.knu");
            WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(20));
            js = (JavascriptExecutor) driver;
            List<DetailedSubject> detailedSubjects = new ArrayList<>();
            Set<String> uniqueCourses = new HashSet<>();

            for (int idx = 0; idx < filtered.size(); idx++) {
                Subject s = filtered.get(idx);
                wait2.until(ExpectedConditions.presenceOfElementLocated(By.id("schEstblYear___input")));

                WebElement yearInput = driver.findElement(By.id("schEstblYear___input"));
                js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('input'));",
                        yearInput, inputYearFull);

                WebElement semesterSelect = driver.findElement(By.id("schEstblSmstrSctcd"));
                js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change'));",
                        semesterSelect, inputSemester);

                new Select(wait2.until(ExpectedConditions.elementToBeClickable(By.id("schCode"))))
                        .selectByVisibleText("교과목코드");

                WebElement inputBox = driver.findElement(By.id("schCodeContents"));
                inputBox.clear();
                inputBox.sendKeys(s.code);

                driver.findElement(By.id("btnSearch")).click();
                wait2.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tbody[@id='grid01_body_tbody']/tr[1]")));

                WebElement scrollDiv = driver.findElement(By.id("grid01_scrollY_div"));
                double scrollTop = 0, scrollHeight = ((Number) js.executeScript("return arguments[0].scrollHeight;", scrollDiv)).doubleValue();
                double clientHeight = ((Number) js.executeScript("return arguments[0].clientHeight;", scrollDiv)).doubleValue();
                double increment = 150;
                boolean newFound;
                boolean isLast = (idx == filtered.size() - 1);

                do {
                    js.executeScript("arguments[0].scrollTop=arguments[1];", scrollDiv, scrollTop);
                    Thread.sleep(800);
                    List<WebElement> rows = driver.findElements(By.xpath("//tbody[@id='grid01_body_tbody']/tr"));
                    int rowCount = isLast ? rows.size() : rows.size() - 1;

                    newFound = false;
                    for (int i = 0; i < rowCount; i++) {
                        List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));
                        if (cells.size() < 17) continue;

                        String key = getCourseKey(cells);
                        if (!uniqueCourses.contains(key)) {
                            uniqueCourses.add(key);
                            String yearVal = getCellText(cells.get(3));
                            String code = getCellText(cells.get(7));
                            if(code.equals("")) continue;
                            String lectureTime = getCellText(cells.get(13));
                            String classroom = getCellText(cells.get(15));
                            String roomNumber = getCellText(cells.get(16));
                            String professor = getCellText(cells.get(12));
                            detailedSubjects.add(new DetailedSubject(yearVal, s.semester, s.division,
                                    code, s.name, s.isRequired, s.isDesign, s.credit,
                                    professor, lectureTime, classroom, roomNumber));
                            newFound = true;
                        }
                    }

                    if (scrollTop + clientHeight >= scrollHeight) break;
                    scrollTop = Math.min(scrollTop + increment, scrollHeight - clientHeight);
                } while (newFound);
            }

            System.out.println("\n=== 상세 강의계획서 정보 ===");
            for (DetailedSubject ds : detailedSubjects) System.out.println(ds.getFormattedInfo());
         // 평점 적용
            RatingUpdater.applyRatings(detailedSubjects, "course_rating.txt");

            // 결과 출력
            System.out.println("\n=== 평점 적용 결과 ===");
            for (DetailedSubject ds : detailedSubjects) {
                System.out.printf("%s - 평점: %.2f\n", ds.getFormattedInfo(), ds.rating);
            }


            while (true) {
                System.out.print("검색할 교과목명을 입력하세요 (종료: exit): ");
                String keyword = scanner.nextLine().trim();
                if (keyword.equalsIgnoreCase("exit")) break;

                List<DetailedSubject> keywordSearchResults = new ArrayList<>();
                Set<String> keywordSearchKeys = new HashSet<>();

                driver.get("https://knuin.knu.ac.kr/public/stddm/lectPlnInqr.knu");
                wait2.until(ExpectedConditions.presenceOfElementLocated(By.id("schEstblYear___input")));

                WebElement yearInput = driver.findElement(By.id("schEstblYear___input"));
                js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('input'));",
                        yearInput, inputYearFull);

                WebElement semesterSelect = driver.findElement(By.id("schEstblSmstrSctcd"));
                js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change'));",
                        semesterSelect, inputSemester);

                new Select(wait2.until(ExpectedConditions.elementToBeClickable(By.id("schCode"))))
                        .selectByVisibleText("교과목명");

                WebElement inputBox = driver.findElement(By.id("schCodeContents"));
                inputBox.clear();
                inputBox.sendKeys(keyword);
                driver.findElement(By.id("btnSearch")).click();

                wait2.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody[@id='grid01_body_tbody']/tr")));

                WebElement scrollDiv = driver.findElement(By.id("grid01_scrollY_div"));
                long lastScrollTop = -1;

                while (true) {
                    List<WebElement> rows = driver.findElements(By.xpath("//tbody[@id='grid01_body_tbody']/tr"));
                    for (WebElement row : rows) {
                        List<WebElement> cells = row.findElements(By.tagName("td"));
                        if (cells.size() < 17) continue;

                        String key = getCourseKey(cells);
                        if (!keywordSearchKeys.contains(key)) {
                            keywordSearchKeys.add(key);
                            String yearVal = getCellText(cells.get(3));
                            String semesterVal = getCellText(cells.get(2));
                            String divisionVal = getCellText(cells.get(4));
                            String code = getCellText(cells.get(7));
                            if (code.isEmpty()) continue;
                            String name = getCellText(cells.get(8));
                            String credit = getCellText(cells.get(9));
                            String professor = getCellText(cells.get(12));
                            String lectureTime = getCellText(cells.get(13));
                            String classroom = getCellText(cells.get(15));
                            String roomNumber = getCellText(cells.get(16));

                            keywordSearchResults.add(new DetailedSubject(
                                yearVal, semesterVal, divisionVal, code, name, credit,
                                professor, lectureTime, classroom, roomNumber));
                        }
                    }

                    js.executeScript("arguments[0].scrollTop += 320;", scrollDiv);
                    Thread.sleep(200);
                    long newScrollTop = (Long) js.executeScript("return arguments[0].scrollTop;", scrollDiv);
                    if (newScrollTop == lastScrollTop) break;
                    lastScrollTop = newScrollTop;
                }

                System.out.println("\n=== 검색 결과 출력 (" + keyword + ") ===");
                if (keywordSearchResults.isEmpty()) {
                    System.out.println("검색 결과가 없습니다.");
                } else {
                    for (DetailedSubject ds : keywordSearchResults) System.out.println(ds.getFormattedInfo());
                }
            }

            driver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
