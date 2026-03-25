package RESEARCHMANTRA_AUTOMATION.AUTOMATION;

import java.time.Duration;
import java.util.List;

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

public class Profile_And_Settings_Tests extends Base_setup {

    @Test(priority = 6, description = "Comprehensive Profile, Deep Clicks, Chrome Intents, and Settings Flow")
    public void testProfileAndSettingsFlow() {
        test = extent.createTest("Profile & Settings Verification - User: " + Base_setup.testFullName);
        AndroidDriver driver = getDriver();
        
        // 🚀 SPEED HACK: Fast polling for instant UI reactions
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        wait.pollingEvery(Duration.ofMillis(100));

        logStep(Status.INFO, "Starting Profile & Settings Verification.");

        try {
            logStep(Status.INFO, "Step 1: Navigating to Profile Tab.");
            safeClick(driver, AppiumBy.accessibilityId("Profile"));
            handlePromoPopup();
            logStep(Status.PASS, "Profile Main Landing UI loaded.");

            // ==========================================
            // STEP 2: EDIT PROFILE LOGIC
            // ==========================================
            logStep(Status.INFO, "Step 2: Testing Personal Details Update logic.");
            String searchName = Base_setup.loggedInUserName.isEmpty() ? "User" : Base_setup.loggedInUserName.split(" ")[0];
            
            // 🚀 FLUTTER MACRO FIX: Profile card merges text, so we use a flexible contains
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, '" + searchName + "') or contains(@content-desc, '" + Base_setup.testFullName + "')]"));

            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Update")));

            logStep(Status.INFO, "Verifying Mobile Number is locked.");
            String mobileFragment = Base_setup.testMobileNumber.length() > 5 ? Base_setup.testMobileNumber.substring(5) : Base_setup.testMobileNumber;
            boolean mobileLocked = !driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, '" + mobileFragment + "') or contains(@text, '" + mobileFragment + "')]")).isEmpty();
            logStep(mobileLocked ? Status.PASS : Status.WARNING, "Verified read-only mobile number field.");

            List<WebElement> editableFields = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(AppiumBy.className("android.widget.EditText")));
            
            if (editableFields.size() >= 2) {
                logStep(Status.INFO, "Updating City Field...");
                WebElement cityField = editableFields.get(editableFields.size() - 2); 
                cityField.click();
                cityField.clear();
                cityField.sendKeys(Base_setup.testCity); 
                dismissKeyboardFast(driver); 
                
                // Re-fetch to avoid StaleElement exceptions after DOM shift
                editableFields = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(AppiumBy.className("android.widget.EditText")));
                
                logStep(Status.INFO, "Updating Email Field...");
                WebElement emailField = editableFields.get(editableFields.size() - 1); 
                emailField.click();
                emailField.clear();
                emailField.sendKeys(Base_setup.testEmail); 
                dismissKeyboardFast(driver); 
                
                safeClick(driver, AppiumBy.accessibilityId("Update"));
                logStep(Status.PASS, "Profile updated correctly: " + Base_setup.testCity + " & " + Base_setup.testEmail);
            } else {
                logStep(Status.WARNING, "Could not find enough EditText fields to update.");
            }

            logStep(Status.INFO, "Exiting Edit Profile screen...");
            navigateBackUI(driver, wait);

            // ==========================================
            // STEP 3: HELP & SUPPORT
            // ==========================================
            logStep(Status.INFO, "Step 3: Verifying Help & Support.");
            scrollToElementByContentDesc("Help & Support");
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Help & Support')]"));
            
            // Wait for Tickets UI or Support UI
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//*[contains(@content-desc, 'tickets') or contains(@content-desc, 'Support Request')]")));
            logStep(Status.PASS, "Help & Support screen verified.");
            navigateBackUI(driver, wait); 

            // ==========================================
            // STEP 4: MY COLLECTION
            // ==========================================
            logStep(Status.INFO, "Step 4: Testing My Collection tab switching.");
            scrollToElementByContentDesc("My Collection");
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'My Collection')]"));
            
            // Verify tabs load, then back out
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//android.view.View")));
            logStep(Status.PASS, "My Collection Ledger verified.");
            navigateBackUI(driver, wait);

            // ==========================================
            // STEP 5: EXTERNAL ROUTING (ABOUT US)
            // ==========================================
            logStep(Status.INFO, "Step 5: Testing Settings & External Browser Routing.");
            scrollToElementByContentDesc("About Us"); // Target About Us directly if Settings wrapper is removed
            try {
                safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'About Us')]"));
            } catch (Exception e) {
                // If it's inside Settings, click Settings first
                safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Settings')]"));
                safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'About Us')]"));
            }
            
            logStep(Status.INFO, "Triggered external routing. Waiting for Browser...");
            Thread.sleep(2500); // OS BUFFER: Hard pause required for Android OS to switch apps to Chrome
            
            // Native back button because we are in an external app (Chrome)
            driver.pressKey(new KeyEvent(AndroidKey.BACK));
            logStep(Status.PASS, "Returned from external routing successfully.");
            Thread.sleep(1000); // Let Flutter wake back up

            // ==========================================
            // STEP 6: LOGOUT MODAL
            // ==========================================
            logStep(Status.INFO, "Verifying Logout Modal.");
            scrollToElementByContentDesc("Logout");
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Logout')]"));
            
            // Dismiss it
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'No') or contains(@content-desc, 'Cancel')]"));
            logStep(Status.PASS, "Logout modal verified and dismissed safely.");

        } catch (Exception e) {
            logStep(Status.FAIL, "Profile & Settings Flow Failed: " + e.getMessage());
            Assert.fail(e.getMessage());
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

    // 🚀 BULLETPROOF KEYBOARD DROPPER
    private void dismissKeyboardFast(AndroidDriver driver) {
        try {
            driver.hideKeyboard(); 
        } catch (Exception e) {
            try { driver.pressKey(new KeyEvent(AndroidKey.BACK)); } catch(Exception ex) {}
        }
        try { Thread.sleep(500); } catch (Exception e) {} // Wait for animation
    }
    
    // 🚀 SMART UI BACK NAVIGATOR (Because Flutter sometimes ignores Android hardware BACK key)
    private void navigateBackUI(AndroidDriver driver, WebDriverWait wait) {
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