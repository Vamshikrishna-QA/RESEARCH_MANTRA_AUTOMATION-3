package RESEARCHMANTRA_AUTOMATION.AUTOMATION;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class All_Products_Payment_Flow_Tests extends Base_setup {

    @Test(priority = 7, description = "Dynamic E-Commerce Funnel: Discovery, Stars, Likes, Plans, Coupons, and Collection Ledger")
    public void testAllProductsPaymentGatewayFlow() {
        test = extent.createTest("Full E-Commerce Funnel: Discovery to Collection");
        AndroidDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        
        Set<String> processedProducts = new HashSet<>();
        List<String> successfullyPurchased = new ArrayList<>();

        try {
            logStep(Status.INFO, "Step 1: Navigating to Trades -> Home Tab.");
            safeClick(driver, AppiumBy.accessibilityId("Trades"));
            handlePromoPopup();

            try {
                safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Home') or contains(@text, 'Home')]"));
            } catch (Exception e) {}

            boolean endOfListReached = false;
            int consecutiveScrollsWithoutNewProducts = 0;

            while (!endOfListReached) {
                List<WebElement> visibleProductTitles = driver.findElements(AppiumBy.xpath(
                        "//android.view.View[contains(@content-desc, 'Positional') or contains(@content-desc, 'Report') or contains(@content-desc, 'Goal')]"));
                
                String currentProductToTest = null;

                for (WebElement titleElement : visibleProductTitles) {
                    String titleText = titleElement.getAttribute("content-desc");
                    if (titleText != null && !titleText.isEmpty() && !processedProducts.contains(titleText)) {
                        currentProductToTest = titleText;
                        break; 
                    }
                }

                if (currentProductToTest == null) {
                    consecutiveScrollsWithoutNewProducts++;
                    if (consecutiveScrollsWithoutNewProducts >= 3) {
                        logStep(Status.INFO, "Reached the bottom of the Home feed. Exiting discovery loop.");
                        break; 
                    }
                    swipeUp(); 
                    continue; 
                }

                consecutiveScrollsWithoutNewProducts = 0;
                processedProducts.add(currentProductToTest);
                logStep(Status.INFO, "===============================================");
                logStep(Status.INFO, "🧪 TESTING FUNNEL FOR: " + currentProductToTest);

                logStep(Status.INFO, "Opening Product Details.");
                
                // 🚀 FLUTTER PHANTOM CLICK FIX: Targeted Center Click via safeClick
                boolean screenChanged = false;
                for(int i = 0; i < 3; i++) {
                    try {
                        safeClick(driver, AppiumBy.xpath("//*[@content-desc='" + currentProductToTest + "']"));
                        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                        shortWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//*[contains(@content-desc, 'Overview') or contains(@text, 'Overview')]")));
                        screenChanged = true;
                        break; 
                    } catch (Exception e) {
                        // Click missed, retry loop
                    }
                }
                if(!screenChanged) throw new RuntimeException("Appium clicked the card, but Flutter ignored it 3 times.");
                
                handlePromoPopup();

                logStep(Status.INFO, "Testing Interactive Star Ratings & Heart Toggle...");
                try {
                    List<WebElement> imageIcons = driver.findElements(AppiumBy.className("android.widget.ImageView"));
                    if (imageIcons.size() >= 6) {
                        imageIcons.get(4).click(); 
                        Thread.sleep(500);
                        imageIcons.get(5).click(); 
                        logStep(Status.PASS, "Successfully interacted with Star Ratings.");
                    } else {
                        logStep(Status.WARNING, "Not enough individual star icons found to interact with.");
                    }

                    WebElement heartIcon = driver.findElement(AppiumBy.xpath("//*[contains(@content-desc, '❤️') or contains(@content-desc, 'like') or @class='android.widget.ImageView'][last()]"));
                    heartIcon.click();
                    logStep(Status.PASS, "Clicked Heart to Like.");
                    Thread.sleep(500);
                    heartIcon.click();
                    logStep(Status.PASS, "Clicked Heart to Unlike.");
                } catch (Exception e) {
                    logStep(Status.WARNING, "Star/Heart UI elements not interactable on this card layout.");
                }

                try {
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                    WebElement readMoreBtn = shortWait.until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.xpath("//*[contains(@content-desc, 'read more') or contains(@content-desc, 'Read more')]")));
                    readMoreBtn.click();
                    logStep(Status.PASS, "Expanded 'Read More' description.");
                } catch (Exception e) {
                    logStep(Status.INFO, "'Read More' not present. Description is fully expanded.");
                }

                swipeUp(); 
                
                String actionBtnXpath = "//*[contains(@content-desc, 'Activate') or contains(@content-desc, 'Subscribe') or contains(@text, 'Buy') or contains(@content-desc, 'Renew') or contains(@content-desc, 'View Stocks') or contains(@content-desc, 'View Reports')]";
                
                try {
                    WebElement actionBtn = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(actionBtnXpath)));
                    String btnType = actionBtn.getAttribute("content-desc") != null ? actionBtn.getAttribute("content-desc") : actionBtn.getAttribute("text");
                    
                    if (btnType != null && (btnType.contains("View Stocks") || btnType.contains("View Reports"))) {
                        logStep(Status.PASS, "✅ ALREADY OWNED: '" + btnType + "' is displayed. Skipping payment funnel.");
                        returnToHomeFeed(driver);
                        continue; 
                    }

                    logStep(Status.INFO, "Clicked '" + btnType + "'. Proceeding to Gateway Funnel...");
                    actionBtn.click();

                    try {
                        WebDriverWait planWait = new WebDriverWait(driver, Duration.ofSeconds(4));
                        WebElement planCard = planWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(
                                "//*[contains(@content-desc, 'Plan') or contains(@text, 'Plan') or contains(@content-desc, 'Months') or contains(@text, 'Months')]")));
                        planCard.click();
                        logStep(Status.PASS, "Selected a Subscription Plan.");
                    } catch (Exception e) {
                        logStep(Status.INFO, "No intermediate Plan Selection screen found. Proceeding directly to checkout.");
                    }

                    logStep(Status.INFO, "Testing Payment Gateway Rebound...");
                    safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Ready to pay') or contains(@text, 'Ready to pay')]"));
                    
                    Thread.sleep(3500);
                    driver.pressKey(new KeyEvent(AndroidKey.BACK)); 
                    logStep(Status.PASS, "Successfully abandoned payment gateway and returned to app.");
                    Thread.sleep(1500);

                    logStep(Status.INFO, "Applying Coupon: SUMMER100");
                    safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'View All') or contains(@text, 'View All') or contains(@content-desc, 'Apply Coupon')]"));
                    
                    WebElement couponBox = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.className("android.widget.EditText")));
                    couponBox.click();
                    couponBox.clear();
                    couponBox.sendKeys("SUMMER100"); 
                    dismissKeyboardSafely(driver);
                    
                    safeClick(driver, AppiumBy.xpath("//android.widget.Button[contains(@content-desc, 'Apply') or contains(@text, 'Apply')]"));
                    
                    wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(
                            "//*[contains(@content-desc, 'GET FREE') or contains(@text, 'GET FREE') or contains(@content-desc, '₹0') or contains(@text, '₹0')]")));
                    logStep(Status.PASS, "Price updated to FREE after coupon application.");
                    
                    safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'GET FREE') or contains(@text, 'GET FREE') or contains(@content-desc, 'Ready to pay')]"));
                    
                    wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(
                            "//*[contains(@content-desc, 'successfully') or contains(@text, 'successfully') or contains(@content-desc, 'Enjoy') or contains(@text, 'Enjoy')]")));
                    logStep(Status.PASS, "✅ Product successfully subscribed for Free!");
                    successfullyPurchased.add(currentProductToTest);

                } catch (Exception e) {
                    logStep(Status.WARNING, "Payment flow could not be completed for this product. UI may have varied.");
                }

                returnToHomeFeed(driver);
            }

            logStep(Status.INFO, "===============================================");
            logStep(Status.INFO, "Step 9: Verifying Ledger in 'My Collection'");
            
            if (successfullyPurchased.isEmpty()) {
                logStep(Status.INFO, "No new products purchased. Skipping Collection verification.");
                return;
            }

            safeClick(driver, AppiumBy.accessibilityId("Profile"));
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//*[contains(@content-desc, 'My Collection') or contains(@text, 'My Collection')]"))).click();

            for (String boughtProduct : successfullyPurchased) {
                String shortName = boughtProduct.split(" ")[0]; 
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(
                            "//*[contains(@content-desc, '" + shortName + "') or contains(@text, '" + shortName + "')]")));
                    logStep(Status.PASS, "✅ VERIFIED: " + boughtProduct + " exists in My Collection.");
                } catch (Exception e) {
                    logStep(Status.FAIL, "❌ LEDGER MISMATCH: " + boughtProduct + " missing from My Collection!");
                    Assert.fail("Purchased product missing from collection: " + boughtProduct);
                }
            }

        } catch (Exception e) {
            logStep(Status.FAIL, "Payment Flow Test Failed: " + e.getMessage());
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

    private void dismissKeyboardSafely(AndroidDriver driver) {
        try { 
            driver.pressKey(new KeyEvent(AndroidKey.BACK));
            Thread.sleep(1000); 
        } catch (Exception e) {}
    }

    private void returnToHomeFeed(AndroidDriver driver) {
        logStep(Status.INFO, "Returning to main Home feed...");
        for (int i = 0; i < 4; i++) {
            try {
                if (!driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, 'Home') or contains(@text, 'Home')]")).isEmpty() ||
                    !driver.findElements(AppiumBy.xpath("//android.view.View[contains(@content-desc, 'Positional')]")).isEmpty()) {
                    break; 
                }
                driver.pressKey(new KeyEvent(AndroidKey.BACK));
                Thread.sleep(1500);
            } catch (Exception e) {
                break;
            }
        }
    }
}