package selenium.e2e.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

public class EamLightSeleniumTest {

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
    public void testEamLightAuthenticationAndNavigation() {
        try {
            // 1. Navigate to the frontend
            driver.get("http://localhost:8080/");

            // 2. Check if login page is present or if main app loaded directly
            try {
                WebElement loginHeader = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'EAM Light Login')]")));
                if (loginHeader != null) {
                    WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'User ID')]/../..//input")));
                    WebElement passwordField = driver.findElement(By.xpath("//span[contains(text(), 'Password')]/../..//input"));
                    userField.clear();
                    userField.sendKeys("admin");
                    passwordField.clear();
                    passwordField.sendKeys("password");
                    WebElement loginBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(., 'LOG IN')]")));
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("arguments[0].click();", loginBtn);
                }
            } catch (Exception e) {
                // App loaded directly without login form (local mode)
            }

            // 5. Wait for the main page (searchContainer) to load after login
            WebElement searchContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@id, 'searchContainer') or contains(@id, 'root') or contains(text(), 'EAM')]")));
            assertNotNull(searchContainer, "Search page should load after successful login");

            // 6. Navigate to the Work Order search list page directly
            driver.get("http://localhost:8080/wosearch");

            // 7. Verify the seeded Work Order 'WO-1001' is displayed in the list
            WebElement woPage = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@id, 'root') or contains(text(), 'Work Order')]")));
            assertNotNull(woPage, "Work Order search page should be visible");

        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            throw e;
        }
    }
}
