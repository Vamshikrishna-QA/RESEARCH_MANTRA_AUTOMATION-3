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

public class Tools_Screen_Tests extends Base_setup {

    @Test(priority = 5, description = "Verify Tools Tab UI and Open All Calculators (TC-075 to TC-085)")
    public void testToolsScreenFlow() {
        test = extent.createTest("Tools & Calculators Verification - User: " + Base_setup.testFullName);
        AndroidDriver driver = getDriver();
        
        // 🚀 SPEED HACK: Fast polling for instant UI reactions
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        wait.pollingEvery(Duration.ofMillis(100));

        logStep(Status.INFO, "Starting Tools Screen Verification.");

        String[] calculators = {
            "Retirement Plans",
            "SIP Calculator",
            "Risk Reward Calculator",
            "Car Loan", // Shortened from "Car Loan Full payment" to bypass Flutter text wrapping
            "GST Savings", 
            "Home Loan", 
            "Sukanya samriddhi", 
            "Lumpsum", 
            "CAGR Calculator",
            "Salary Budget"
        };

        try {
            // ==========================================
            // 1. Navigate to Tools Tab (High Speed)
            // ==========================================
            logStep(Status.INFO, "Navigating to Tools Tab.");
            safeClick(driver, AppiumBy.xpath("//android.view.View[@content-desc='Tools']"));
            handlePromoPopup();
            
            // 🚀 SMART WAIT: Wait using flexible 'contains' xpath to bypass hidden Flutter characters
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//*[contains(@content-desc, 'Retirement Plans')]")));
            logStep(Status.PASS, "Successfully opened Tools Screen instantly.");

            // ==========================================
            // 2. Iterate, Open, and Close each Calculator
            // ==========================================
            logStep(Status.INFO, "Step 2: High-Speed Navigation Test for " + calculators.length + " Financial Tools.");

            for (String calcName : calculators) {
                try {
                    // Scroll into view safely
                    try {
                        driver.findElement(AppiumBy.androidUIAutomator(
                            "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().descriptionContains(\"" + calcName + "\"));"));
                    } catch (Exception e) {}
                    
                    // 🚀 HUMAN TAP: Click the calculator card
                    safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, '" + calcName + "')]"));
                    
                    // 🚀 SMART WAIT: Every calculator has input boxes, wait for EditText to prove it loaded!
                    wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.className("android.widget.EditText")));
                    logStep(Status.INFO, "Opened: " + calcName);
                    
                    // Navigate Back
                    navigateBackUI(driver);
                    
                    // 🚀 SMART WAIT: Verify we are back on the main Tools menu
                    wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//*[contains(@content-desc, '" + calcName + "')]")));
                    logStep(Status.PASS, "Successfully verified navigation for: " + calcName);
                    
                } catch (Exception e) {
                    logStep(Status.FAIL, "Failed to interact with " + calcName + ". Error: " + e.getMessage());
                    try { navigateBackUI(driver); } catch (Exception ex) {} // Safe recovery
                }
            }

            logStep(Status.PASS, "Tools & Calculators navigation stress test completed lightning fast.");

        } catch (Exception e) {
            logStep(Status.FAIL, "Tools Screen Test Failed: " + e.getMessage());
            Assert.fail("Tools Screen Flow Failure: " + e.getMessage());
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

    // 🚀 SMART UI BACK NAVIGATOR 
    private void navigateBackUI(AndroidDriver driver) {
        try {
            // Attempt 1: Try to click the Flutter UI Back Arrow (Top Left corner)
            WebElement uiBackButton = driver.findElement(AppiumBy.xpath("//android.widget.Button[1]"));
            int centerX = uiBackButton.getRect().getX() + (uiBackButton.getRect().getWidth() / 2);
            int centerY = uiBackButton.getRect().getY() + (uiBackButton.getRect().getHeight() / 2);
            
            org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
            org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(finger, 1);
            tap.addAction(finger.createPointerMove(Duration.ZERO, org.openqa.selenium.interactions.PointerInput.Origin.viewport(), centerX, centerY));
            tap.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(java.util.Collections.singletonList(tap));
            
            Thread.sleep(500); // Transition wait
        } catch (Exception e) {
            // Attempt 2: Fallback to Android Hardware Back Key
            try { 
                driver.pressKey(new KeyEvent(AndroidKey.BACK)); 
                Thread.sleep(800);
            } catch (Exception ex) {}
        }
    }
}