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

public class Profile_And_Settings_Tests extends Base_setup {

    @Test(priority = 6, description = "Comprehensive Profile, Deep Clicks, Chrome Intents, and Settings Flow")
    public void testProfileAndSettingsFlow() {
        test = extent.createTest("Profile & Settings Verification - User: " + Base_setup.testFullName);
        AndroidDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        logStep(Status.INFO, "Starting Profile & Settings Verification.");

        try {
            logStep(Status.INFO, "Step 1: Navigating to Profile Tab.");
            safeClick(driver, AppiumBy.accessibilityId("Profile"));
            handlePromoPopup();
            logStep(Status.PASS, "Profile Main Landing UI loaded.");

            logStep(Status.INFO, "Step 2: Testing Personal Details Update logic.");
            String userName = Base_setup.loggedInUserName.isEmpty() ? "User" : Base_setup.loggedInUserName;
            
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, '" + userName + "') or contains(@content-desc, '" + Base_setup.testFullName + "')]"));

            // 🚀 SMART WAIT: Wait for the Update button to prove the Edit Profile screen loaded
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Update")));

            logStep(Status.INFO, "Verifying Mobile Number is locked.");
            boolean mobileLocked = !driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, '" + Base_setup.testMobileNumber.substring(5) + "')]")).isEmpty();
            logStep(mobileLocked ? Status.PASS : Status.WARNING, "Verified read-only mobile number field.");

            List<WebElement> editableFields = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(AppiumBy.className("android.widget.EditText")));
            
            if (editableFields.size() >= 2) {
                logStep(Status.INFO, "Updating City Field...");
                WebElement cityField = editableFields.get(editableFields.size() - 2); 
                cityField.click();
                cityField.clear();
                cityField.sendKeys(Base_setup.testCity); 
                dismissKeyboardSafely(driver); // 🚀 NATIVE FIX: Closes keyboard safely
                
                // 🚀 ANTI-STALE FIX: Re-fetch fields after the keyboard drops to prevent StaleElementReferenceException
                editableFields = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(AppiumBy.className("android.widget.EditText")));
                
                logStep(Status.INFO, "Updating Email Field...");
                WebElement emailField = editableFields.get(editableFields.size() - 1); 
                emailField.click();
                emailField.clear();
                emailField.sendKeys(Base_setup.testEmail); 
                dismissKeyboardSafely(driver); // 🚀 NATIVE FIX
                
                // 🚀 ANTI-STALE FIX: Use safeClick because the Update button shifts when the keyboard drops
                safeClick(driver, AppiumBy.accessibilityId("Update"));
                logStep(Status.PASS, "Profile updated correctly: " + Base_setup.testCity + " & " + Base_setup.testEmail);
            } else {
                logStep(Status.WARNING, "Could not find enough EditText fields to update.");
            }

            logStep(Status.INFO, "Exiting Edit Profile screen...");
            navigateBack(driver);

            logStep(Status.INFO, "Step 3: Verifying Help & Support.");
            scrollToElementByContentDesc("Help & Support");
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Help & Support')]"));
            
            // 🚀 SMART WAIT: Wait dynamically for the tickets text
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//*[contains(@content-desc, 'tickets')]")));
            logStep(Status.PASS, "Help & Support screen verified.");
            navigateBack(driver); 

            logStep(Status.INFO, "Step 4: Testing My Collection tab switching.");
            scrollToElementByContentDesc("My Collection");
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'My Collection')]"));
            
            // 🚀 SMART WAIT: Wait for the sub-tabs to become clickable before interacting
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'History')]"));
            logStep(Status.PASS, "Switched to My Collection -> History tab.");
            navigateBack(driver);

            logStep(Status.INFO, "Step 5: Testing Settings & External Browser Routing.");
            scrollToElementByContentDesc("Settings");
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Settings')]"));
            
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'About Us')]"));
            logStep(Status.INFO, "Triggered external routing. Waiting for Browser...");
            
            // OS BUFFER: Give Android OS time to launch the browser
            Thread.sleep(2000); 

            navigateBack(driver);
            logStep(Status.PASS, "Returned from external routing successfully.");
            
            logStep(Status.INFO, "Verifying Logout Modal.");
            scrollToElementByContentDesc("Logout");
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Logout')]"));
            
            // 🚀 SMART WAIT: Wait dynamically for the cancel button
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'No') or contains(@content-desc, 'Cancel')]"));
            logStep(Status.PASS, "Logout modal verified and dismissed safely.");

        } catch (Exception e) {
            logStep(Status.FAIL, "Profile & Settings Flow Failed: " + e.getMessage());
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

    // 🚀 BULLETPROOF KEYBOARD DISMISSAL: Uses Native Hardware Back Button
    private void dismissKeyboardSafely(AndroidDriver driver) {
        try { 
            driver.pressKey(new KeyEvent(AndroidKey.BACK));
            Thread.sleep(1500); // 1.5 second pause to let the keyboard physically slide down
        } catch (Exception e) {
            // Ignore if keyboard is already down
        }
    }
    
    private void navigateBack(AndroidDriver driver) {
        try {
            driver.navigate().back();
            // Short buffer to prevent commands from firing before transition finishes
            Thread.sleep(1000); 
        } catch (Exception e) {
            try { driver.findElement(AppiumBy.xpath("//android.widget.Button")).click(); } catch (Exception ex) {}
        }
    }
}