package selenium.e2e.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrontendApplicationE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        System.setProperty("webdriver.chrome.verboseLogging", "true");
        System.setProperty("webdriver.chrome.logfile", "chromedriver.log");

        ChromeDriverService service = new ChromeDriverService.Builder()
                .withVerbose(true)
                .withLogOutput(System.out)
                .build();

        ChromeOptions options = new ChromeOptions();
        // Foreground visible maximized Chrome window
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        options.setExperimentalOption("detach", true);

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        driver = new ChromeDriver(service, options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        EamLightSeleniumTest.login(driver, wait);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            sleep(2000); // Visual pause before closing browser window
            driver.quit();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void takeDiagnostic(String testName) {
        try {
            System.out.println("DIAGNOSTIC [" + testName + "] URL: " + driver.getCurrentUrl());
            WebElement body = driver.findElement(By.tagName("body"));
            String text = body.getAttribute("innerText") + " " + body.getAttribute("textContent");
            System.out.println("DIAGNOSTIC [" + testName + "] BODY: " + (text.length() > 1000 ? text.substring(0, 1000) : text) + "...");
            System.out.println("DIAGNOSTIC [" + testName + "] BROWSER CONSOLE LOGS:");
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
            for (LogEntry entry : logs) {
                System.out.println("  " + entry.getLevel() + " " + entry.getMessage());
            }
        } catch (Exception e) {
            System.out.println("DIAGNOSTIC [" + testName + "] FAILED: " + e.getMessage());
        }
    }

    private void assertNoSevereBrowserErrors(String pageName) {
        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
        List<LogEntry> errorLogs = logEntries.getAll().stream()
                .filter(entry -> entry.getLevel().equals(Level.SEVERE)
                        || entry.getMessage().contains("TypeError")
                        || entry.getMessage().contains("Uncaught")
                        || entry.getMessage().contains("500")
                        || entry.getMessage().contains("SyntaxError")
                        || entry.getMessage().contains("ReferenceError"))
                .collect(Collectors.toList());

        if (!errorLogs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Browser console errors detected on page '").append(pageName).append("':\n");
            for (LogEntry log : errorLogs) {
                sb.append("  [").append(log.getLevel()).append("] ").append(log.getMessage()).append("\n");
            }
            Assertions.fail(sb.toString());
        }
    }

    @Test
    public void testRootPageNoErrors() {
        driver.get(BASE_URL + "/");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Root /");
    }

    @Test
    public void testWorkOrderSearchNoErrors() {
        driver.get(BASE_URL + "/wosearch");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Work Order Search");
    }

    @Test
    public void testAssetSearchNoErrors() {
        driver.get(BASE_URL + "/assetsearch");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Asset Search");
    }

    @Test
    public void testPositionSearchNoErrors() {
        driver.get(BASE_URL + "/positionsearch");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Position Search");
    }

    @Test
    public void testSystemSearchNoErrors() {
        driver.get(BASE_URL + "/systemsearch");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("System Search");
    }

    @Test
    public void testLocationSearchNoErrors() {
        driver.get(BASE_URL + "/locationsearch");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Location Search");
    }

    @Test
    public void testPartSearchNoErrors() {
        driver.get(BASE_URL + "/partsearch");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Part Search");
    }

    @Test
    public void testNCRSearchNoErrors() {
        driver.get(BASE_URL + "/ncrsearch");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("NCR Search");
    }

    @Test
    public void testReplaceEqpNoErrors() {
        driver.get(BASE_URL + "/replaceeqp");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Replace Equipment");
    }

    @Test
    public void testMeterReadingNoErrors() {
        driver.get(BASE_URL + "/meterreading");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Meter Reading");
    }

    @Test
    public void testLotSearchNoErrors() {
        driver.get(BASE_URL + "/lotsearch");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Lot Search");
    }

    @Test
    public void testGridPageNoErrors() {
        driver.get(BASE_URL + "/grid");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Grid Page");
    }

    @Test
    public void testReportPageNoErrors() {
        driver.get(BASE_URL + "/report");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Report Page");
    }

    @Test
    public void testReleaseNotesNoErrors() {
        driver.get(BASE_URL + "/releasenotes");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Release Notes");
    }

    @Test
    public void testEqpTreeNoErrors() {
        driver.get(BASE_URL + "/eqptree");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        assertNotNull(root);
        assertNoSevereBrowserErrors("Equipment Tree");
    }

    @Test
    public void testWorkOrderNewNoErrors() {
        driver.get(BASE_URL + "/workorder/new");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='root']//*[self::input or self::button or contains(@class, 'entityToolbar')]")));
        sleep(1500);
        assertNotNull(root);
        assertNoSevereBrowserErrors("Work Order New");
    }

    @Test
    public void testAssetNewNoErrors() {
        driver.get(BASE_URL + "/asset/new");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='root']//*[self::input or self::button or contains(@class, 'entityToolbar')]")));
        sleep(1500);
        assertNotNull(root);
        assertNoSevereBrowserErrors("Asset New");
    }

    @Test
    public void testPositionNewNoErrors() {
        driver.get(BASE_URL + "/position/new");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='root']//*[self::input or self::button or contains(@class, 'entityToolbar')]")));
        sleep(1500);
        assertNotNull(root);
        assertNoSevereBrowserErrors("Position New");
    }

    @Test
    public void testSystemNewNoErrors() {
        driver.get(BASE_URL + "/system/new");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='root']//*[self::input or self::button or contains(@class, 'entityToolbar')]")));
        sleep(1500);
        assertNotNull(root);
        assertNoSevereBrowserErrors("System New");
    }

    @Test
    public void testLocationNewNoErrors() {
        driver.get(BASE_URL + "/location/new");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='root']//*[self::input or self::button or contains(@class, 'entityToolbar')]")));
        sleep(1500);
        assertNotNull(root);
        assertNoSevereBrowserErrors("Location New");
    }

    @Test
    public void testPartNewNoErrors() {
        driver.get(BASE_URL + "/part/new");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='root']//*[self::input or self::button or contains(@class, 'entityToolbar')]")));
        sleep(1500);
        assertNotNull(root);
        assertNoSevereBrowserErrors("Part New");
    }

    @Test
    public void testNCRNewNoErrors() {
        driver.get(BASE_URL + "/ncr/new");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='root']//*[self::input or self::button or contains(@class, 'entityToolbar')]")));
        sleep(1500);
        assertNotNull(root);
        assertNoSevereBrowserErrors("NCR New");
    }

    @Test
    public void testWorkOrderSubcomponentsNoErrors() {
        driver.get(BASE_URL + "/workorder/10001");
        WebElement root = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='root']//*[self::input or self::button or contains(@class, 'entityToolbar')]")));
        sleep(1500);
        assertNotNull(root);
        assertNoSevereBrowserErrors("Work Order Subcomponents");
    }

    @Test
    public void testUserAutocompleteNoErrors() {
        driver.get(BASE_URL + "/rest/autocomplete/users/ADMIN");
        sleep(1000);
        assertNoSevereBrowserErrors("User Autocomplete Endpoint");
    }

    public static void main(String[] args) {
        setupClass();
        FrontendApplicationE2ETest test = new FrontendApplicationE2ETest();
        try {
            System.out.println("--- Starting Visual Interactive E2E Test ---");

            System.out.println("1. Testing Work Order Creation Page...");
            test.setupTest();
            test.testWorkOrderNewNoErrors();
            test.teardown();

            System.out.println("2. Testing Part Creation Page...");
            test.setupTest();
            test.testPartNewNoErrors();
            test.teardown();

            System.out.println("3. Testing Asset Creation Page...");
            test.setupTest();
            test.testAssetNewNoErrors();
            test.teardown();

            System.out.println("--- All Visual E2E Tests Completed Successfully! ---");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
