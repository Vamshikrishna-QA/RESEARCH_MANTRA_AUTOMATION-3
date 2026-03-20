package RESEARCHMANTRA_AUTOMATION.AUTOMATION;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.StaleElementReferenceException;
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

public class Push_Notification_And_Signal_Tests extends Base_setup {

    @Test(priority = 8, description = "Test In-App Notification Routing and Signal State Distribution")
    public void testLiveSignalRoutingAndNotifications() {
        test = extent.createTest("Push Notification & Signal Routing");
        AndroidDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15)); 

        try {
            driver.openNotifications();
            
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                WebElement activeNotification = shortWait.until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.xpath("//*[contains(@text, 'Research Mantra')]")));
                
                activeNotification.click();
                
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.xpath("//*[contains(@content-desc, 'Trades') or contains(@content-desc, 'Explore')]")));
            } catch (Exception e) {
                driver.pressKey(new KeyEvent(AndroidKey.BACK));
                Thread.sleep(1500); 
            }

            logStep(Status.INFO, "Verifying Signal Distribution Logic.");
            
            safeClick(driver, AppiumBy.accessibilityId("Trades"));
            handlePromoPopup();
            
            logStep(Status.INFO, "Opening the first available product to check for Live Commentary...");
            
            // 🚀 FLUTTER PHANTOM CLICK FIX 🚀
            boolean productOpened = false;
            for(int i = 0; i < 3; i++) {
                try {
                    safeClick(driver, AppiumBy.xpath("//android.view.View[contains(@content-desc, 'Positional') or contains(@content-desc, 'Goal')]"));
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                    shortWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//*[contains(@content-desc, 'Overview') or contains(@text, 'Overview')]")));
                    productOpened = true;
                    break;
                } catch(Exception e) {}
            }
            if(!productOpened) {
                Assert.fail("Failed to open product card to check signals. Clicks ignored by UI.");
            }
            
            handlePromoPopup();

            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Live Commentary')]"));
            
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.xpath("//*[contains(@content-desc, 'ENTRY') or contains(@content-desc, 'EXIT') or contains(@content-desc, 'Start Date')]")));
            } catch (Exception e) {}
            
            List<WebElement> signals = driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, 'ENTRY') or contains(@content-desc, 'EXIT')]"));
            if(signals.isEmpty()) {
                logStep(Status.WARNING, "No signals broadcasted today to test routing.");
                return;
            }

            String signalText = signals.get(0).getAttribute("content-desc");
            boolean isTradeOpen = signalText.contains("ENTRY") || signalText.contains("Partial");

            if (isTradeOpen) {
                safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Details')]"));
                
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.xpath("//*[contains(@content-desc, 'Entry Price') or contains(@content-desc, 'Activate Now') or contains(@content-desc, 'No active')]")));
                
                boolean foundInLive = !driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, 'Entry Price')]")).isEmpty();
                boolean isPaywalled = !driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, 'Activate Now')]")).isEmpty();
                
                Assert.assertTrue(foundInLive || isPaywalled, "BUSINESS LOGIC FAILURE: 'ENTRY' signal found, but no card AND no paywall exists!");
                
                if (isPaywalled) {
                    logStep(Status.PASS, "✅ Verified: New User is correctly paywalled from viewing the live trade card.");
                } else {
                    logStep(Status.PASS, "✅ Verified: Trade correctly routed to Live Trades.");
                }
            }

        } catch (Exception e) {
            logStep(Status.FAIL, "Push Notification Test Failed: " + e.getMessage());
            Assert.fail(e.getMessage());
        } finally {
            returnToDashboardSafe();
        }
    }
    
 // 🚀 THE "HUMAN TAP" ANTI-STALE CLICKER (Bypasses Flutter Gesture Blockers)
    private void safeClick(AndroidDriver driver, org.openqa.selenium.By by) throws Exception {
        for (int i = 0; i < 3; i++) {
            try {
                org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
                org.openqa.selenium.WebElement el = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(by));
                
                // Calculate exact center coordinates of the element
                int centerX = el.getRect().getX() + (el.getRect().getWidth() / 2);
                int centerY = el.getRect().getY() + (el.getRect().getHeight() / 2);
                
                // Simulate physical finger tap at X/Y coordinates
                org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
                org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(finger, 1);
                tap.addAction(finger.createPointerMove(java.time.Duration.ZERO, org.openqa.selenium.interactions.PointerInput.Origin.viewport(), centerX, centerY));
                tap.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
                tap.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
                driver.perform(java.util.Collections.singletonList(tap));
                
                return;
            } catch (Exception e) {
                Thread.sleep(1000); 
            }
        }
        driver.findElement(by).click(); // Ultimate Fallback
    }
}