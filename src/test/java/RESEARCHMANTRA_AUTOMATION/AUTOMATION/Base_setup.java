package RESEARCHMANTRA_AUTOMATION.AUTOMATION;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;
import java.util.List;
import java.util.Collections;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

public class Base_setup {

    private static ThreadLocal<AndroidDriver> driver = new ThreadLocal<>();
    private ThreadLocal<Long> testStartTime = new ThreadLocal<>();
    
    public static Properties prop;
    public static ExtentReports extent;
    public static ExtentSparkReporter spark;
    public static ExtentTest test;

    public AndroidDriver getDriver() {
        return driver.get();
    }

    public static String testMobileNumber = ""; 
    public static String testFullName = "";
    public static String testEmail = "";
    public static String testCity = "";
    public static String loggedInUserName = "";

    public Base_setup() {
        try {
            prop = new Properties();
            String configPath = System.getProperty("user.dir") + File.separator + "config.properties";
            FileInputStream fis = new FileInputStream(configPath);
            prop.load(fis);
            fis.close();
            
            String activeUser = prop.getProperty("active.user.id");
            if (activeUser != null) {
                testMobileNumber = prop.getProperty("user." + activeUser + ".mobile");
                testFullName = prop.getProperty("user." + activeUser + ".name");
                testEmail = prop.getProperty("user." + activeUser + ".email");
                testCity = prop.getProperty("user." + activeUser + ".city");
            }
        } catch (Exception e) {
            Reporter.log("❌ CONFIG LOAD ERROR: " + e.getMessage(), true);
        }
    }

    @BeforeSuite
    public void setupSuite() {
        String reportPath = System.getProperty("user.dir") + "/Reports/ResearchMantra_Report.html";
        spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("QA Engineer", "Vamshi Krishna");

        try {
            UiAutomator2Options options = new UiAutomator2Options()
                    .setPlatformName(prop.getProperty("platform.name"))
                    .setDeviceName(prop.getProperty("device.name"))
                    .setAppPackage(prop.getProperty("app.package"))
                    .setAppActivity(prop.getProperty("app.activity"))
                    .setAppWaitActivity("*") 
                    .setAutoGrantPermissions(true)
                    .setFullReset(true) 
                    .setNewCommandTimeout(Duration.ofSeconds(3600));

            // PERFORMANCE CONFIGURATIONS (Safe for Flutter)
            options.setCapability("appium:disableWindowAnimation", true);
            options.setCapability("appium:skipServerInstallation", false);

            if (prop.getProperty("app.path") != null) options.setApp(prop.getProperty("app.path"));

            Reporter.log("\n⏳ APPIUM IS CLEANING DEVICE AND INSTALLING FRESH APK...", true);
            driver.set(new AndroidDriver(new URL(prop.getProperty("appium.server.url")), options));
            
            // 🚀 STABILITY RESTORED: Standard 15-second implicit wait allows the app to breathe
            getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            
            Reporter.log("✅ APPIUM SESSION ESTABLISHED - STABILITY MODE ENGAGED!", true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeMethod
    public void beforeTestLog(Method method) {
        Reporter.log("\n========================================================", true);
        Reporter.log("🚀 STARTING TEST: " + method.getName(), true);
        Reporter.log("========================================================", true);
        testStartTime.set(System.currentTimeMillis());
    }

    @AfterMethod
    public void getResult(ITestResult result) {
        long duration = System.currentTimeMillis() - testStartTime.get();
        if(result.getStatus() == ITestResult.FAILURE) {
            logStep(Status.FAIL, "❌ Test Case Failed: " + result.getName());
            try {
                String base64Screenshot = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.BASE64);
                test.fail("Snapshot", MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
            } catch (Exception e) {}
        } else if(result.getStatus() == ITestResult.SUCCESS) {
            logStep(Status.PASS, "✅ Test Case Passed: " + result.getName());
        }
        logStep(Status.INFO, "⏱️ SCREEN COMPLETED IN: " + (duration / 1000.0) + " SECONDS.");
        returnToDashboardSafe();
    }

    @AfterSuite
    public void tearDownSuite() {
        if (getDriver() != null) {
            try {
                getDriver().removeApp(prop.getProperty("app.package"));
            } catch (Exception e) {
                // Ignore if app already removed
            }
            getDriver().quit();
        }
        extent.flush();
    }

    public void logStep(Status status, String message) {
        Reporter.log("  -> " + message, true); 
        if (test != null) test.log(status, message);     
    }

    public void dismissSystemNotifications() {
        try {
            Dimension size = getDriver().manage().window().getSize();
            int startX = size.width / 2;
            int startY = (int) (size.height * 0.15); 
            int endY = 10; 

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(), startX, endY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            getDriver().perform(Collections.singletonList(swipe));
        } catch (Exception e) {}
    }

    public void handlePromoPopup() {
        try {
            List<WebElement> gift = getDriver().findElements(AppiumBy.xpath("//*[contains(@content-desc, 'Gift') or contains(@content-desc, 'ACTIVATE')]"));
            if (!gift.isEmpty()) {
                logStep(Status.WARNING, "🎁 Gift Popup Blocked the UI! Clicking ACTIVATE...");
                gift.get(0).click();
                new WebDriverWait(getDriver(), Duration.ofSeconds(3))
                    .until(ExpectedConditions.invisibilityOfAllElements(gift));
            }
        } catch (Exception e) {}
    }

    public void verifyToastMessage(String expectedText) {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(5));
            WebElement toast = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//android.widget.Toast")));
            String actualText = toast.getAttribute("name");
            logStep(Status.INFO, "Captured Toast: " + actualText);
            Assert.assertTrue(actualText.contains(expectedText), "Toast text mismatch!");
            logStep(Status.PASS, "Negative Validation successful.");
        } catch (Exception e) {
            Assert.fail("Toast message '" + expectedText + "' did not appear.");
        }
    }

    public void returnToDashboardSafe() {
        dismissSystemNotifications(); 
        handlePromoPopup();           
        
        try {
            boolean home = !getDriver().findElements(AppiumBy.xpath("//*[contains(@content-desc, 'Explore Now')]")).isEmpty();
            if (home) { Reporter.log("  -> ✅ Already on Home Dashboard.", true); return; }
            
            new WebDriverWait(getDriver(), Duration.ofSeconds(3))
                .until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Explore"))).click();
        } catch (Exception e) {
            for (int i = 0; i < 3; i++) {
                try {
                    getDriver().navigate().back();
                    Thread.sleep(800);
                    if (!getDriver().findElements(AppiumBy.accessibilityId("Explore")).isEmpty()) {
                        getDriver().findElement(AppiumBy.accessibilityId("Explore")).click();
                        break;
                    }
                } catch (Exception ex) {}
            }
        }
    }

    public void scrollToElementByContentDesc(String contentDesc) {
        try {
            getDriver().findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView("
                + "new UiSelector().descriptionContains(\"" + contentDesc + "\"));"));
        } catch (Exception e) {}
    }

    public boolean verifyElementIsDisplayed(WebDriverWait wait, String name, String xpath) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(xpath))).isDisplayed();
        } catch (Exception e) { return false; }
    }
    
    public void swipeUp() {
        try {
            Dimension size = getDriver().manage().window().getSize();
            int startX = size.width / 2, startY = (int) (size.height * 0.8), endY = (int) (size.height * 0.3);
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(), startX, endY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            getDriver().perform(Collections.singletonList(swipe));
        } catch (Exception e) {}
    }

    public void pullToRefresh() {
        try {
            Dimension size = getDriver().manage().window().getSize();
            int startX = size.width / 2, startY = (int) (size.height * 0.25), endY = (int) (size.height * 0.75);   

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), startX, endY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            
            getDriver().perform(Collections.singletonList(swipe));
            Thread.sleep(1500); 
            logStep(Status.INFO, "🔄 Performed Pull-to-Refresh to sync latest signals.");
        } catch (Exception e) {
            logStep(Status.WARNING, "Pull-to-refresh gesture failed.");
        }
    }
}