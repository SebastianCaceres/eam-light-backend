package selenium.e2e.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PartE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            try {
                LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
                System.out.println("====== BROWSER CONSOLE LOGS ======");
                for (LogEntry entry : logEntries) {
                    System.out.println(entry.getTimestamp() + " " + entry.getLevel() + ": " + entry.getMessage());
                }
                System.out.println("==================================");
            } catch (Exception e) {
                System.err.println("Could not retrieve browser logs: " + e.getMessage());
            }
            driver.quit();
        }
    }

    @Test
    public void testPartSearchAndDetailNavigation() {
        try {
            driver.get("http://localhost:8080/partsearch");
            WebElement searchContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@id, 'searchContainer') or contains(@class, 'searchContainer') or contains(text(), 'Part')]")));
            assertNotNull(searchContainer, "Part search page should load");
        } catch (Exception e) {
            System.err.println("testPartSearchAndDetailNavigation failed: " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testLotSearchAndDetailNavigation() {
        try {
            driver.get("http://localhost:8080/lotsearch");
            WebElement searchContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@id, 'searchContainer') or contains(@class, 'searchContainer') or contains(text(), 'Lot')]")));
            assertNotNull(searchContainer, "Lot search page should load");
        } catch (Exception e) {
            System.err.println("testLotSearchAndDetailNavigation failed: " + e.getMessage());
            throw e;
        }
    }
}
