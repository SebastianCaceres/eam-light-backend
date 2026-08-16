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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecordCommitE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080";

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
            driver.quit();
        }
    }

    private List<LogEntry> captureAndAssertNoSevereConsoleErrors(String testName) {
        List<LogEntry> severeErrors = new ArrayList<>();
        try {
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            System.out.println("====== BROWSER CONSOLE LOGS [" + testName + "] ======");
            for (LogEntry entry : logEntries) {
                System.out.println(entry.getTimestamp() + " [" + entry.getLevel() + "]: " + entry.getMessage());
                if (entry.getLevel().equals(Level.SEVERE)) {
                    // Ignore expected harmless 404/favicon or websocket debug messages
                    if (!entry.getMessage().contains("favicon.ico") && !entry.getMessage().contains("ws://")) {
                        severeErrors.add(entry);
                    }
                }
            }
            System.out.println("======================================================");
        } catch (Exception e) {
            System.err.println("Could not retrieve browser logs: " + e.getMessage());
        }
        return severeErrors;
    }

    private void fillDescriptionAndSave(String pageUrl, String entityType, String testName) throws Exception {
        driver.get(BASE_URL + pageUrl);

        // 1. Wait for page container & toolbar
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root, entityType + " page should load");

        // 2. Find and fill description field
        Thread.sleep(1500);
        List<WebElement> inputs = driver.findElements(By.xpath("//textarea | //input[@type='text']"));
        assertTrue(!inputs.isEmpty(), "Should find input fields on " + entityType + " form");

        WebElement descInput = null;
        for (WebElement input : inputs) {
            if (input.isDisplayed() && input.isEnabled()) {
                descInput = input;
                break;
            }
        }
        assertNotNull(descInput, "Should locate visible description field for " + entityType);

        String testDesc = "E2E Commitment " + entityType + " " + System.currentTimeMillis();
        EamLightSeleniumTest.setReactInput(driver, descInput, testDesc);

        // 3. Click Save button in Toolbar
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Save') or contains(., 'SAVE')]")));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", saveBtn);

        // 4. Wait for save operation to communicate with backend
        Thread.sleep(2500);

        // 5. Capture and assert zero severe browser errors
        List<LogEntry> severeErrors = captureAndAssertNoSevereConsoleErrors(testName);
        assertTrue(severeErrors.isEmpty(), "Expected 0 SEVERE browser console errors during " + entityType + " save, but found: " + severeErrors);
    }

    @Test
    public void testCreateAndCommitWorkOrder() throws Exception {
        fillDescriptionAndSave("/workorder", "WorkOrder", "testCreateAndCommitWorkOrder");
    }

    @Test
    public void testCreateAndCommitAsset() throws Exception {
        fillDescriptionAndSave("/asset", "Asset", "testCreateAndCommitAsset");
    }

    @Test
    public void testCreateAndCommitPart() throws Exception {
        fillDescriptionAndSave("/part", "Part", "testCreateAndCommitPart");
    }
}
