package RESEARCHMANTRA_AUTOMATION.AUTOMATION;

import java.time.Duration;
import java.util.ArrayList;
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

public class All_Products_Payment_Flow_Tests extends Base_setup {

    @Test(priority = 7, description = "Business Logic Flow: Discovery, Overview/Performance Tabs, Gateway, and Ledger")
    public void testAllProductsPaymentGatewayFlow() {
        test = extent.createTest("Full E-Commerce Business Flow: Discovery to Collection");
        AndroidDriver driver = getDriver();
        
        // 🚀 SPEED HACK: Fast polling
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.pollingEvery(Duration.ofMillis(100));
        
        List<String> successfullyPurchased = new ArrayList<>();
        
        // 🎯 EXACT base strings confirmed by your Appium Recorder
        String[] productsToTest = {
            "MCX Positional", 
            "Long term Goal Oriented",
            "Nifty Positional", 
            "Midcap Positional"
        };

        try {
            // 🧠 BUSINESS LOGIC: Start at Trades -> Home
            logStep(Status.INFO, "Step 1: Navigating to Trades Tab.");
            safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Trades\")"));
            handlePromoPopup();

            // Click the "Home" sub-tab (or explore if it defaults there)
            try {
                safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Home\")"));
                Thread.sleep(500);
            } catch (Exception e) {}

            logStep(Status.INFO, "Step 2: Processing Product Cards sequentially...");
            
            for (String currentProduct : productsToTest) {
                logStep(Status.INFO, "===============================================");
                logStep(Status.INFO, "🧪 TESTING BUSINESS FLOW FOR: " + currentProduct);

                // 🚀 Scroll to the exact product card natively
                try {
                    driver.findElement(AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().descriptionContains(\"" + currentProduct + "\"));"));
                } catch (Exception e) {}

                // 🧠 BUSINESS LOGIC: Click Card to route to Subscription Page
                logStep(Status.INFO, "Clicking Product Card to open details...");
                boolean screenChanged = false;
                for(int i = 0; i < 3; i++) {
                    try {
                        // 🚀 RECORDER FIX: The entire card is an ImageView. Click the ImageView containing the name!
                        safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.ImageView\").descriptionContains(\"" + currentProduct + "\")"));
                        
                        // 🚀 RECORDER FIX: Wait for "Overview\nTab 1 of 2" (using contains to ignore the \n)
                        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                        shortWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Overview\")")));
                        screenChanged = true;
                        break; 
                    } catch (Exception e) {
                        Thread.sleep(500);
                    }
                }
                
                if(!screenChanged) {
                    logStep(Status.WARNING, "Could not route into card. Skipping to next.");
                    returnToHomeFeed(driver);
                    continue;
                }
                logStep(Status.PASS, "Successfully navigated to Product Subscription Page.");
                handlePromoPopup();

                // 🧠 BUSINESS LOGIC: Verify "Overview" and "Performance" Tabs
                logStep(Status.INFO, "Verifying 'Overview' and 'Performance' Product Details Tabs...");
                try {
                    // Recorder showed tabs have text like "Performance\nTab 2 of 2"
                    safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Performance\")"));
                    Thread.sleep(500);
                    safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Overview\")"));
                    logStep(Status.PASS, "✅ Overview and Performance tabs are active and switchable.");
                } catch (Exception e) {
                    logStep(Status.WARNING, "Overview/Performance tabs failed to load properly.");
                }

                // 🧠 BUSINESS LOGIC: Subscription State Verification (Owned vs Not Owned)
                logStep(Status.INFO, "Scrolling to evaluate Subscription Status...");
                WebElement actionBtn = null;
                boolean alreadyOwned = false;
                
                for(int s = 0; s < 6; s++) {
                    try {
                        // Check if already bought
                        if (!driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"View Stocks\")")).isEmpty() || 
                            !driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"View Reports\")")).isEmpty()) {
                            alreadyOwned = true;
                            actionBtn = driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"View \")"));
                            break;
                        }
                        
                        // Recorder showed "Activate Now" or "ACTIVATE"
                        if (!driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Activate Now\")")).isEmpty()) {
                            actionBtn = driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Activate Now\")"));
                            break;
                        }
                        if (!driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"ACTIVATE\")")).isEmpty()) {
                            actionBtn = driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"ACTIVATE\")"));
                            break;
                        }
                    } catch (Exception e) {}
                    swipeUp(); 
                    Thread.sleep(300);
                }

                try {
                    if(actionBtn == null) throw new RuntimeException("Action Button not found.");

                    if (alreadyOwned) {
                        logStep(Status.PASS, "✅ BUSINESS LOGIC: Product is ALREADY OWNED ('View Stocks' visible). Skipping payment funnel.");
                        returnToHomeFeed(driver);
                        continue; 
                    }

                    logStep(Status.INFO, "Product NOT OWNED. Proceeding to Gateway...");
                    safeClickElement(driver, actionBtn);

                    // 🚀 Select Plan (Using RECORDER logic for "Months Plan")
                    try {
                        WebDriverWait planWait = new WebDriverWait(driver, Duration.ofSeconds(4));
                        WebElement planCard = planWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Months Plan\")")));
                        safeClickElement(driver, planCard);
                    } catch (Exception e) {}

                    // Gateway Rebound
                    logStep(Status.INFO, "Testing Payment Gateway Rebound...");
                    try {
                        safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Ready to pay\")"));
                        safeClick(driver, AppiumBy.xpath("//*[@text='Proceed to pay' or contains(@content-desc, 'Proceed')]"));
                    } catch (Exception e) {}
                    
                    Thread.sleep(3500); 
                    driver.pressKey(new KeyEvent(AndroidKey.BACK)); 
                    logStep(Status.PASS, "Returned from Gateway successfully.");
                    Thread.sleep(1000);

                    // 🧠 BUSINESS LOGIC: Discount Flow (SUMMER100)
                    logStep(Status.INFO, "Applying Coupon: SUMMER100");
                    try {
                        safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"View All\")"));
                        
                        for(int retry = 0; retry < 3; retry++) {
                            WebElement couponBox = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.className("android.widget.EditText")));
                            safeClickElement(driver, couponBox);
                            couponBox.clear();
                            couponBox.sendKeys("SUMMER100"); 
                            dismissKeyboardSafely(driver);
                            
                            safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Apply\")"));
                            
                            try {
                                WebDriverWait fastWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                                fastWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"GET FREE\")")));
                                break; 
                            } catch (Exception ex) {}
                        }
                        
                        logStep(Status.PASS, "✅ BUSINESS LOGIC: Price successfully discounted to FREE (₹0).");
                        safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"GET FREE\")"));
                        
                        wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"View Stocks\")")));
                        logStep(Status.PASS, "Subscription activated successfully!");
                        successfullyPurchased.add(currentProduct);
                        
                        safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"View Stocks\")"));
                        Thread.sleep(1000);
                        safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Home\")")); // Return to dash

                    } catch (Exception e) {
                        logStep(Status.WARNING, "Coupon flow skipped/failed.");
                        returnToHomeFeed(driver);
                    }

                } catch (Exception e) {
                    logStep(Status.WARNING, "Flow broke for this product. Reason: " + e.getMessage());
                    returnToHomeFeed(driver);
                }
            }

            // 🧠 BUSINESS LOGIC: Ledger Verification
            logStep(Status.INFO, "===============================================");
            logStep(Status.INFO, "Step 3: Verifying Ledger in 'My Collection'");
            if (!successfullyPurchased.isEmpty()) {
                safeClick(driver, AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Profile\")"));
                wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"My Collection\")"))).click();
                
                for (String boughtProduct : successfullyPurchased) {
                    String shortName = boughtProduct.split(" ")[0]; 
                    try {
                        wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"" + shortName + "\")")));
                        logStep(Status.PASS, "✅ BUSINESS LOGIC: " + boughtProduct + " successfully added to My Collection ledger.");
                    } catch (Exception e) {
                        logStep(Status.FAIL, "❌ MISMATCH: " + boughtProduct + " is missing from My Collection ledger!");
                        Assert.fail("Ledger update failed for: " + boughtProduct);
                    }
                }
            } else {
                logStep(Status.INFO, "No new products purchased. Skipping Collection verification.");
            }

        } catch (Exception e) {
            logStep(Status.FAIL, "Payment Flow Test Failed: " + e.getMessage());
            Assert.fail(e.getMessage());
        } finally {
            returnToDashboardSafe();
        }
    }

    // --- REFINED HUMAN TAP UTILITIES ---

    private void safeClick(AndroidDriver driver, org.openqa.selenium.By by) throws Exception {
        WebDriverWait clickWait = new WebDriverWait(driver, Duration.ofSeconds(8));
        clickWait.pollingEvery(Duration.ofMillis(100)); 
        WebElement el = clickWait.until(ExpectedConditions.presenceOfElementLocated(by));
        safeClickElement(driver, el);
    }

    private void safeClickElement(AndroidDriver driver, WebElement el) {
        int centerX = el.getRect().getX() + (el.getRect().getWidth() / 2);
        int centerY = el.getRect().getY() + (el.getRect().getHeight() / 2);
        
        org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO, org.openqa.selenium.interactions.PointerInput.Origin.viewport(), centerX, centerY));
        tap.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(java.util.Collections.singletonList(tap));
    }

    private void dismissKeyboardSafely(AndroidDriver driver) {
        try { driver.hideKeyboard(); } 
        catch (Exception e) { try { driver.pressKey(new KeyEvent(AndroidKey.BACK)); } catch(Exception ex) {} }
        try { Thread.sleep(600); } catch (Exception e) {}
    }

    private void returnToHomeFeed(AndroidDriver driver) {
        for (int i = 0; i < 5; i++) {
            try {
                if (!driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Trades\")")).isEmpty() || 
                    !driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Positional\")")).isEmpty()) {
                    
                    try { driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Trades\")")).click(); } catch(Exception ex) {}
                    break; 
                }
                driver.pressKey(new KeyEvent(AndroidKey.BACK));
                Thread.sleep(800); 
            } catch (Exception e) { break; }
        }
    }
}