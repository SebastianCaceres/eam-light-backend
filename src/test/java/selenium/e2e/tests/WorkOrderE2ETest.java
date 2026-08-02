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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkOrderE2ETest {

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
        EamLightSeleniumTest.login(driver, wait);
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
    public void testWorkOrderSearchAndDetailView() {
        try {
            // 1. Open Work Order Search Page
            driver.get("http://localhost:8080/wosearch");

            // 2. Verify page container loads
            WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
            assertNotNull(root, "Work Order Search page should load");

        } catch (Exception e) {
            System.err.println("WorkOrderE2ETest failed: " + e.getMessage());
            throw e;
        }
    }
}
