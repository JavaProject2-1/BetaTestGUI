package basicWeb;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

public class Curriculum {

    static class Subject {
        String year;
        String semester;
        String division;
        String code;
        String name;
        boolean isRequired;
        boolean isDesign;
        String credit;

        public Subject(String year, String semester, String division, String code, String name,
                       boolean isRequired, boolean isDesign, String credit) {
            this.year = year;
            this.semester = semester;
            this.division = division;
            this.code = code;
            this.name = name;
            this.isRequired = isRequired;
            this.isDesign = isDesign;
            this.credit = credit;
        }

        @Override
        public String toString() {
            return String.format("%s학년 %s [%s] %s - %s, 필수: %s, 설계: %s, 학점: %s", year, semester, division, code, name, isRequired, isDesign, credit);
        }
    }

    public static void main(String[] args) {
        // ✅ chromedriver 자동 경로 설정
        String currentPath = Paths.get("").toAbsolutePath().toString();
        String driverPath = currentPath + File.separator + "chromedriver";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            driverPath += ".exe";
        }
        System.setProperty("webdriver.chrome.driver", driverPath);

        WebDriver driver = new ChromeDriver();
        Subject[] subjects = new Subject[500];
        int subjectCount = 0;

        try {
            driver.get("https://cse.knu.ac.kr/sub3_2_b.php");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("tbody")));

            String year = "";
            String division = "";

            for (WebElement tbody : driver.findElements(By.tagName("tbody"))) {
                for (WebElement row : tbody.findElements(By.tagName("tr"))) {
                    var ths = row.findElements(By.tagName("th"));
                    var tds = row.findElements(By.tagName("td"));

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
                    if (year.equals("")) {
                        continue;
                    }

                    // 왼쪽 셀 = 1학기
                    if (tds.size() >= 3) {
                        Subject sub = parseSubject(year, "1학기", division, tds.get(0), tds.get(1), tds.get(2));
                        if (sub != null && subjectCount < subjects.length) subjects[subjectCount++] = sub;
                    }

                    // 오른쪽 셀 = 2학기
                    if (tds.size() >= 6) {
                        Subject sub = parseSubject(year, "2학기", division, tds.get(3), tds.get(4), tds.get(5));
                        if (sub != null && subjectCount < subjects.length) subjects[subjectCount++] = sub;
                    }
                }
            }

            if (subjectCount == 0) {
                System.out.println("❌ 저장된 과목이 없습니다. 필터 조건 또는 HTML 구조를 확인하세요.");
            } else {
                System.out.println("✅ 저장된 과목 수: " + subjectCount);
                for (int i = 0; i < subjectCount; i++) {
                    System.out.println(subjects[i]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private static Subject parseSubject(String year, String semester, String division,
                                        WebElement codeElem, WebElement nameElem, WebElement creditElem) {
        String code = codeElem.getText().trim();
        String html = nameElem.getAttribute("innerHTML");
        String name = html.replaceAll("<[^>]*>", "").split("\\(")[0].trim();
        String credit = creditElem.getText().trim().replaceAll("\\s+", "");

        if (code.isBlank() || code.equals("-") || credit.isBlank() || credit.equals("-")) return null;
        if (name.isBlank() || name.equals("-")) return null;

        boolean isRequired = html.contains("bum01");
        boolean isDesign = html.contains("bum02");

        return new Subject(year, semester, division, code, name, isRequired, isDesign, credit);
    }
}
