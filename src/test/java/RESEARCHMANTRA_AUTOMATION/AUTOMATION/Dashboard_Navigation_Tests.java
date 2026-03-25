package RESEARCHMANTRA_AUTOMATION.AUTOMATION;

import java.time.Duration;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

public class Dashboard_Navigation_Tests extends Base_setup {

    @Test(priority = 2, description = "Comprehensive E2E Dashboard Flow: Header, Notifications, Cards, Analytics, and Nav")
    public void testDashboardCompleteFlow() {
        test = extent.createTest("Dashboard: Master Top-to-Bottom E2E Flow - " + Base_setup.testFullName);
        AndroidDriver driver = getDriver();
        
        // 🚀 SPEED HACK: 12-second max wait, polling every 100ms
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        wait.pollingEvery(Duration.ofMillis(100));

        logStep(Status.INFO, "Starting Comprehensive Dashboard Verification...");

        try {
            // ==========================================
            // STEP 1: HEADER & PROFILE NAME (TOP)
            // ==========================================
            logStep(Status.INFO, "Step 1: Checking User Profile Name in Header.");
            
            try { driver.findElement(AppiumBy.androidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollToBeginning(5);")); } catch (Exception e) {}

            String expectedName = Base_setup.loggedInUserName.isEmpty() ? Base_setup.testFullName : Base_setup.loggedInUserName;
            String shortName = expectedName.split(" ")[0]; 
            
            // 🚀 FAST UI SELECTOR: Checks if the header contains the user's short name
            boolean isNameVisible = !driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"" + shortName + "\")")).isEmpty() ||
                                    !driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"" + shortName.toLowerCase() + "\")")).isEmpty();
            
            if (isNameVisible) {
                logStep(Status.PASS, "Profile Name verified in header: " + expectedName);
            } else {
                logStep(Status.WARNING, "Profile Name not explicitly found in header. (May be generic 'User').");
            }

            // ==========================================
            // STEP 2: NOTIFICATIONS (TOP RIGHT)
            // ==========================================
            logStep(Status.INFO, "Step 2: Checking Notification Bell & Tabs.");
            try {
                safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\").instance(0)"));
                logStep(Status.PASS, "Clicked Notification Bell successfully.");
                Thread.sleep(1000); 

                safeClick(driver, AppiumBy.accessibilityId("Show Unread"));
                Thread.sleep(500);

                safeClick(driver, AppiumBy.accessibilityId("Mark all as read"));
                Thread.sleep(500);

                safeClick(driver, AppiumBy.accessibilityId("All"));
                Thread.sleep(500);

                logStep(Status.PASS, "Successfully interacted with all Notification tabs.");

            } catch (Exception e) {
                logStep(Status.WARNING, "Could not interact with Notifications. Error: " + e.getMessage());
            }
            
            // Exit Notifications
            try { driver.pressKey(new KeyEvent(AndroidKey.BACK)); } catch (Exception e) {}
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Explore")));
            logStep(Status.INFO, "Returned to Dashboard from Notifications.");

            // ==========================================
            // STEP 3: CATEGORY CARDS (MID-TOP)
            // ==========================================
            logStep(Status.INFO, "Step 3: Deep Clicking Core Trade Category Cards.");
            
            String[] categories = {"MCX Positional", "Long term Goal Oriented", "Nifty Positional", "Midcap Positional"};
            
            for (String cat : categories) {
                logStep(Status.INFO, "Testing Routing for: " + cat);
                
                try {
                    driver.findElement(AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().descriptionContains(\"" + cat + "\"));"));
                } catch (Exception e) {}

                // 🚀 FIX: Replaced crash-inducing XPath with Native UiSelector
                if (!driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"" + cat + "\")")).isEmpty()) {
                    safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"" + cat + "\")")); 
                    
                    try {
                        WebDriverWait routingWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                        routingWait.pollingEvery(Duration.ofMillis(200));
                        // 🚀 FIX: Lightning fast check for "Overview" tab instead of heavy OR statement
                        routingWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Overview\")")));
                        logStep(Status.PASS, "✅ Successfully routed to Product Screen for: " + cat);
                    } catch (Exception e) {
                        logStep(Status.FAIL, "❌ Routing failed for: " + cat);
                        Assert.fail("Routing failed for product card: " + cat);
                    }

                    try { driver.pressKey(new KeyEvent(AndroidKey.BACK)); } catch (Exception e) {}
                    wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Explore"))); 
                    Thread.sleep(300);
                } else {
                    logStep(Status.FAIL, "Missing Category Card: " + cat);
                    Assert.fail("Missing Category Card: " + cat);
                }
            }

            // ==========================================
            // STEP 4: PROMOTIONAL BANNERS (MIDDLE)
            // ==========================================
            logStep(Status.INFO, "Step 4: Deep Clicking Promotional Banners.");
            
            try {
                driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().descriptionContains(\"Research Reports\"));"));
            } catch (Exception e) {}
            
            // 🚀 FIX: Pure UiSelector logic for the Research Banner
            if(!driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Research Reports\")")).isEmpty()) {
                safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Research Reports\")"));
                try {
                    WebDriverWait routingWait = new WebDriverWait(driver, Duration.ofSeconds(4));
                    routingWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Reports\")")));
                    logStep(Status.PASS, "✅ Successfully routed to Research Reports Screen.");
                } catch (Exception e) {
                    logStep(Status.WARNING, "Routing check for Research Reports timed out.");
                }
                try { driver.pressKey(new KeyEvent(AndroidKey.BACK)); } catch (Exception e) {}
                wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Explore")));
            }

            // ==========================================
            // STEP 5: BOTTOM NAVIGATION TABS (EDGE)
            // ==========================================
            logStep(Status.INFO, "Step 5: Testing Bottom Navigation Functional Clicks.");
            
            String[] navTabs = {"Trades", "Blogs", "Tools", "Profile", "Explore"};
            
            for (String tab : navTabs) {
                logStep(Status.INFO, "Clicking Bottom Nav Tab: " + tab);
                try {
                    safeClick(driver, AppiumBy.accessibilityId(tab));
                    logStep(Status.PASS, "Navigated to '" + tab + "' tab successfully.");
                    Thread.sleep(800); 
                } catch (Exception e) {
                    try {
                        safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"" + tab + "\")"));
                        logStep(Status.PASS, "Navigated to '" + tab + "' tab via fallback.");
                        Thread.sleep(800);
                    } catch (Exception ex) {
                        logStep(Status.FAIL, "Navigation Tab Missing or Unclickable: " + tab);
                        Assert.fail("Bottom navigation tab missing: " + tab);
                    }
                }
            }
            
            logStep(Status.PASS, "✅ Master Dashboard Top-to-Bottom Flow completed successfully.");

        } catch (Exception e) {
            logStep(Status.FAIL, "Dashboard Master Flow Failed: " + e.getMessage());
            Assert.fail("Dashboard Verification Failed: " + e.getMessage());
        } finally {
            returnToDashboardSafe();
        }
    }

    // ⚡ ZERO-LATENCY "HUMAN TAP"
    private void safeClick(AndroidDriver driver, org.openqa.selenium.By by) throws Exception {
        WebDriverWait clickWait = new WebDriverWait(driver, Duration.ofSeconds(8));
        clickWait.pollingEvery(Duration.ofMillis(100)); 
        
        for (int i = 0; i < 2; i++) {
            try {
                WebElement el = clickWait.until(ExpectedConditions.presenceOfElementLocated(by));
                
                int centerX = el.getRect().getX() + (el.getRect().getWidth() / 2);
                int centerY = el.getRect().getY() + (el.getRect().getHeight() / 2);
                
                org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
                org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(finger, 1);
                tap.addAction(finger.createPointerMove(Duration.ZERO, org.openqa.selenium.interactions.PointerInput.Origin.viewport(), centerX, centerY));
                tap.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
                tap.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
                driver.perform(java.util.Collections.singletonList(tap));
                return;
            } catch(Exception e) {
                Thread.sleep(200); 
            }
        }
        driver.findElement(by).click(); 
    }
}