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

public class Trades_Screen_Tests extends Base_setup {

    @Test(priority = 3, description = "Dynamically Verify All Products: Subscription States, Free Access Tabs, and Empty Data")
    public void testDynamicTradesScreenFlow() {
        test = extent.createTest("Dynamic Trades Module Flow - User: " + Base_setup.testFullName);
        AndroidDriver driver = getDriver();
        
        // 🚀 SPEED HACK: Fast polling for instant UI reactions
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.pollingEvery(Duration.ofMillis(100));

        logStep(Status.INFO, "Starting Dynamic Trades Screen Verification.");

        try {
            logStep(Status.INFO, "Navigating to Trades Bottom Tab.");
            safeClick(driver, AppiumBy.xpath("//android.view.View[@content-desc='Trades']"));
            handlePromoPopup();

            // ========================================================================
            // STEP 1: DYNAMICALLY SCRAPE PRODUCT NAMES (INSTANT)
            // ========================================================================
            logStep(Status.INFO, "Step 1: Extracting dynamic product list from the app...");
            List<String> dynamicProducts = new ArrayList<>();

            WebElement horizontalMenu = wait.until(ExpectedConditions
                    .presenceOfElementLocated(AppiumBy.className("android.widget.HorizontalScrollView")));
            List<WebElement> productChips = horizontalMenu.findElements(AppiumBy.className("android.view.View"));

            for (WebElement chip : productChips) {
                String productName = chip.getAttribute("content-desc");
                if (productName != null && !productName.trim().isEmpty() && !productName.contains("Tab")) {
                    dynamicProducts.add(productName.trim());
                }
            }

            logStep(Status.PASS,
                    "Dynamically found " + dynamicProducts.size() + " products: " + dynamicProducts.toString());
            if (dynamicProducts.isEmpty())
                Assert.fail("No products found in the horizontal menu! UI structure may have changed.");

            // ========================================================================
            // STEP 2: ITERATE AT HIGH SPEED WITH BUSINESS LOGIC CHECKS
            // ========================================================================
            for (String product : dynamicProducts) {
                logStep(Status.INFO, "===============================================");
                logStep(Status.INFO, "🧪 TESTING PRODUCT: " + product);

                // --- A. HOME TAB ---
                logStep(Status.INFO, "-> Checking Home Tab for: " + product);
                clickTopTab(driver, wait, "Home");
                handlePromoPopup();
                verifyDynamicServiceCard(driver, product);

                // --- B. LIVE TRADES TAB ---
                logStep(Status.INFO, "-> Checking Live Trades Tab for: " + product);
                clickTopTab(driver, wait, "Live Trades");
                clickProductChip(driver, wait, product);
                handlePromoPopup();

                logStep(Status.INFO, "   -> Verifying 'Details' Sub-Tab (Subscription Logic).");
                clickSubTab(driver, wait, "Details");
                verifyLiveTradeDetails(driver, wait, product);

                logStep(Status.INFO, "   -> Verifying 'Live Commentary' Sub-Tab (Free Access).");
                clickSubTab(driver, wait, "Live Commentary");
                verifyLiveCommentary(driver, wait);

                // --- C. CLOSED TRADES TAB ---
                logStep(Status.INFO, "-> Checking Closed Trades Tab for: " + product + " (Free Access)");
                clickTopTab(driver, wait, "Closed Trades");
                clickProductChip(driver, wait, product);
                handlePromoPopup();
                verifyClosedTradeDetails(driver, wait);

                logStep(Status.PASS, "✅ Full verification complete for: " + product);
            }

            logStep(Status.PASS, "Dynamic Trades Screen Verification completed lightning fast.");

        } catch (Exception e) {
            logStep(Status.FAIL, "Trades Screen Test Failed: " + e.getMessage());
            Assert.fail("Trades Screen Failure: " + e.getMessage());
        } finally {
            returnToDashboardSafe();
        }
    }

    // ==========================================
    // HIGH-SPEED HELPER METHODS 
    // ==========================================

    private void clickTopTab(AndroidDriver driver, WebDriverWait wait, String tabName) {
        try {
            safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, '" + tabName + "') or contains(@text, '" + tabName + "')]"));
            Thread.sleep(500); // Allow Flutter tab animation to finish
        } catch (Exception e) {
            logStep(Status.WARNING, "Could not click top tab: " + tabName);
        }
    }

    private void clickSubTab(AndroidDriver driver, WebDriverWait wait, String subTabName) {
        try {
            safeClick(driver, AppiumBy.xpath("//android.view.View[contains(@content-desc, '" + subTabName + "')]"));
            Thread.sleep(500);
        } catch (Exception e) {
            logStep(Status.WARNING, "Could not click sub-tab: " + subTabName);
        }
    }

    private void clickProductChip(AndroidDriver driver, WebDriverWait wait, String productName) {
        try {
            try {
                // Horizontal scroll to find chip
                driver.findElement(AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().className(\"android.widget.HorizontalScrollView\")).setAsHorizontalList().scrollIntoView(new UiSelector().descriptionContains(\""
                                + productName + "\"));"));
            } catch (Exception e) {}

            safeClick(driver, AppiumBy.xpath("//android.widget.HorizontalScrollView//android.view.View[contains(@content-desc, '" + productName + "')]"));
            Thread.sleep(500);
        } catch (Exception e) {
            logStep(Status.WARNING, "Could not locate or click product chip: " + productName);
        }
    }

    private void verifyDynamicServiceCard(AndroidDriver driver, String productName) {
        boolean isCardVisible = false;
        String strictCardXPath = "//*[contains(@content-desc, '" + productName
                + "') and not(contains(@content-desc, 'Tab')) and not(@content-desc='Explore') and not(@content-desc='Trades')]";

        for (int i = 0; i < 4; i++) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                shortWait.pollingEvery(Duration.ofMillis(100)); // Fast polling
                shortWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(strictCardXPath)));
                isCardVisible = true;
                break;
            } catch (Exception e) {
                swipeUp();
            }
        }

        if (isCardVisible)
            logStep(Status.PASS, "Found Home Service Card for: " + productName);
        else {
            logStep(Status.FAIL, "Could not find Home Service Card for: " + productName);
            Assert.fail("Service Card validation failed for: " + productName);
        }

        try {
            driver.findElement(AppiumBy.androidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollToBeginning(5);"));
        } catch (Exception e) {}
    }

    // 🧠 BUSINESS LOGIC: Verifies Subscription State (Locked vs Empty vs Data)
    private void verifyLiveTradeDetails(AndroidDriver driver, WebDriverWait wait, String productName) {
        try {
            // Wait for the UI area to load any valid state
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(
                    "//*[contains(@content-desc, 'Entry Price') or contains(@content-desc, 'Current Price') or contains(@content-desc, 'No active') or contains(@content-desc, 'Activate Now') or contains(@content-desc, 'Subscribe')]"
            )));

            // Dynamically detect the business state
            boolean isLocked = !driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, 'Activate Now') or contains(@content-desc, 'Subscribe')]")).isEmpty();
            boolean isEmpty = !driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, 'No active')]")).isEmpty();
            boolean hasData = !driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, 'Entry Price') or contains(@content-desc, 'Current Price')]")).isEmpty();

            if (isLocked) {
                logStep(Status.WARNING, "🔒 Product [" + productName + "] is NOT PURCHASED. 'Activate Now' lock screen is correctly displayed.");
            } else if (isEmpty) {
                logStep(Status.INFO, "✅ Product [" + productName + "] is PURCHASED, but no active trades are currently running (Empty State).");
            } else if (hasData) {
                logStep(Status.PASS, "✅ Product [" + productName + "] is PURCHASED. Active trade signals are visible!");
            } else {
                // 🚀 APPLIED OPTIMIZATION: Fail the test if no known business state is detected
                logStep(Status.FAIL, "❌ Product [" + productName + "] is in an UNKNOWN STATE! UI may be broken or loading failed.");
                Assert.fail("Unknown business state for Live Trades product: " + productName);
            }
        } catch (Exception e) {
            logStep(Status.FAIL, "Live Trades UI is blank or unrecognizable.");
            Assert.fail("Live Trades UI check failed.");
        }
    }

    // 🧠 BUSINESS LOGIC: Free Access Verification (Data vs Empty)
    private void verifyLiveCommentary(AndroidDriver driver, WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(
                    "//*[contains(@content-desc, 'Start Date') or contains(@content-desc, 'ENTRY') or contains(@content-desc, 'No commentary') or contains(@content-desc, 'No data') or contains(@content-desc, 'No Live Commentary')]"
            )));

            boolean hasData = !driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, 'Start Date') or contains(@content-desc, 'ENTRY')]")).isEmpty();
            
            if (hasData) {
                logStep(Status.PASS, "✅ Live Commentary feed loaded with free access data.");
            } else {
                logStep(Status.INFO, "✅ Live Commentary loaded successfully, but no recent updates are available (Empty State).");
            }
        } catch (Exception e) {
            logStep(Status.FAIL, "Live Commentary feed is blank or unrecognizable.");
            Assert.fail("Live Commentary UI check failed.");
        }
    }

    // 🧠 BUSINESS LOGIC: Free Access Verification (Booked Trades vs Empty)
    private void verifyClosedTradeDetails(AndroidDriver driver, WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(
                    "//*[contains(@content-desc, 'Profit') or contains(@content-desc, 'Loss') or contains(@content-desc, 'No closed trades') or contains(@content-desc, 'No data')]"
            )));
            
            boolean hasData = !driver.findElements(AppiumBy.xpath("//*[contains(@content-desc, 'Profit') or contains(@content-desc, 'Loss')]")).isEmpty();

            if (hasData) {
                logStep(Status.PASS, "✅ Closed Trades (Booked Trades) loaded with free access data.");
            } else {
                logStep(Status.INFO, "✅ Closed Trades UI loaded successfully, but history is empty (No booked trades).");
            }
        } catch (Exception e) {
            logStep(Status.FAIL, "Closed Trades UI had issues rendering.");
            Assert.fail("Closed Trades UI check failed.");
        }
    }
    
    // ⚡ ZERO-LATENCY "HUMAN TAP" (Flutter Safe)
    private void safeClick(AndroidDriver driver, org.openqa.selenium.By by) throws Exception {
        WebDriverWait clickWait = new WebDriverWait(driver, Duration.ofSeconds(8));
        clickWait.pollingEvery(Duration.ofMillis(100)); 
        
        for (int i = 0; i < 2; i++) {
            try {
                // 🚀 CRITICAL: Uses presenceOfElementLocated so Flutter cannot block the tap
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
                Thread.sleep(200); // Micro-pause before retry
            }
        }
        driver.findElement(by).click(); // Ultimate Fallback
    }
}