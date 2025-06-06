package basicWeb;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class LectureCrawler {

    private static String getCellText(WebElement cell) {
        try {
            WebElement nobr = cell.findElement(By.tagName("nobr"));
            String innerHtml = nobr.getDomProperty("innerHTML");
            String textWithNewlines = innerHtml.replaceAll("(?i)<br[^>]*>", "\n");
            return textWithNewlines.replaceAll("<[^>]+>", "").trim();
        } catch (NoSuchElementException e) {
            return cell.getText().trim();
        }
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
        else if (os.contains("nux") || os.contains("nix")) driverPath = "chromedriver_linux"; // Linux, Unix
        else throw new RuntimeException("지원하지 않는 운영체제입니다: " + os);

        // 절대 경로로 변환
        String absolutePath = Paths.get(driverPath).toAbsolutePath().toString();
        File driverFile = new File(absolutePath);

        // 파일 존재 여부 확인
        if (!driverFile.exists()) {
            throw new RuntimeException("크롬 드라이버 파일을 찾을 수 없습니다: " + absolutePath +
                    "\n다음 경로에 해당 운영체제용 크롬 드라이버를 배치해주세요.");
        }

        // Unix 계열 시스템에서 실행 권한 설정
        if (!os.contains("win")) {
            if (!driverFile.canExecute()) {
                boolean success = driverFile.setExecutable(true);
                if (!success) {
                    System.err.println("경고: 드라이버 파일에 실행 권한을 설정할 수 없습니다: " + absolutePath);
                }
            }
        }

        // 시스템 속성 설정
        System.setProperty("webdriver.chrome.driver", absolutePath);
    }

    public static void searchLectures(String year, String semester, String subject,
                                      DefaultTableModel tableModel, JLabel statusLabel, JButton searchBtn) {

        // 크롬 드라이버 자동 설정
        setupChromeDriver();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://knuin.knu.ac.kr/public/stddm/lectPlnInqr.knu");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            WebElement yearInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schEstblYear___input")));
            js.executeScript("arguments[0].value='" + year + "'; arguments[0].dispatchEvent(new Event('change'));", yearInput);

            WebElement semesterSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[title='개설학기']")));
            new Select(semesterSelect).selectByVisibleText(semester);

            js.executeScript("document.getElementById('schSbjetCd1').value = '';" );

            WebElement detailSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("schCode")));
            new Select(detailSelect).selectByVisibleText("교과목명");

            WebElement inputBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schCodeContents")));
            inputBox.clear();
            inputBox.sendKeys(subject);

            WebElement searchBtnElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnSearch")));
            searchBtnElement.click();

            Thread.sleep(1500);  // 안정성 확보

            WebElement tbody = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody[@id='grid01_body_tbody']")));
            List<WebElement> rows = tbody.findElements(By.tagName("tr"));

            int headerLength = LanguageChange.getHeaders("Korean").length;

            for (WebElement row : rows) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.isEmpty()) continue;

                boolean hasContent = cells.stream().anyMatch(cell -> !getCellText(cell).isEmpty());
                if (!hasContent || cells.size() < headerLength - 1) continue;

                Object[] rowData = new Object[headerLength];
                for (int i = 0; i < headerLength; i++) {
                    if (i < cells.size()) {
                        String text = getCellText(cells.get(i)).replace("\n", " / ").replaceAll(" +", " ").trim();
                        rowData[i] = text;
                    } else {
                        rowData[i] = "";
                    }
                }
                
                // 평점 넣기 (course = 교과목명, professor = 담당교수)
                String course = rowData[8].toString();     // "교과목명"
                String professor = rowData[12].toString(); // "담당교수"
                Double rating = RatingLoader.getRating(course, professor);
                rowData[headerLength - 1] = (rating != null) ? String.format("%.2f", rating) : "-";
                
                tableModel.addRow(rowData);
            }

            statusLabel.setText("검색 완료: " + tableModel.getRowCount() + "건");

        } catch (Exception e) {
            statusLabel.setText("오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
            searchBtn.setEnabled(true);
        }
    }
}
