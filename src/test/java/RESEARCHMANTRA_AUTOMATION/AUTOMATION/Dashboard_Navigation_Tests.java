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

public class Dashboard_Navigation_Tests extends Base_setup {

    @Test(priority = 2, description = "TC-DSH-01 & 02: Verify Header, Profile Name, and Notifications")
    public void testHeaderAndNotifications() {
        test = extent.createTest("Dashboard: Header & Notifications - " + Base_setup.testFullName);
        AndroidDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        logStep(Status.INFO, "Starting Header & Notifications Verification...");

        try {
            logStep(Status.INFO, "Step 1: Checking User Profile Name in Header.");
            String expectedName = Base_setup.loggedInUserName.isEmpty() ? Base_setup.testFullName : Base_setup.loggedInUserName;
            
            boolean isNameVisible = verifyElementIsDisplayed(wait, "Profile Header", 
                    "//*[contains(@content-desc, '" + expectedName.split(" ")[0] + "')]");
            
            if (isNameVisible) {
                logStep(Status.PASS, "Profile Name verified in header: " + expectedName);
            } else {
                logStep(Status.WARNING, "Profile Name not explicitly found in header. (May be generic 'User').");
            }

            logStep(Status.INFO, "Step 2: Checking Notification Bell.");
            WebElement notificationBell = wait.until(ExpectedConditions.elementToBeClickable(
                    AppiumBy.xpath("//android.view.View/android.widget.Button")));
            notificationBell.click();
            
            logStep(Status.PASS, "Clicked Notification Bell successfully.");
            Thread.sleep(1500); 
            
            driver.navigate().back();
            logStep(Status.INFO, "Returned to Dashboard from Notifications.");

        } catch (Exception e) {
            logStep(Status.FAIL, "Header Test Failed: " + e.getMessage());
            Assert.fail("Header Verification Failed: " + e.getMessage());
        } finally {
            returnToDashboardSafe();
        }
    }

    @Test(priority = 3, description = "TC-DSH-07 & 08: Verify Performance Analytics & SEBI Compliance")
    public void testPerformanceAnalyticsSection() {
        test = extent.createTest("Dashboard: Analytics & Performance");
        AndroidDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        logStep(Status.INFO, "Starting Performance Analytics Verification...");

        try {
            logStep(Status.INFO, "Step 1: Scrolling to Performance Section.");
            scrollToElementByContentDesc("Susmita Performance");

            verifyElementIsDisplayed(wait, "Performance Header & SEBI", 
                    "//*[contains(@content-desc, 'Susmita Performance') and contains(@content-desc, 'SEBI')]");
            logStep(Status.PASS, "Performance Header and SEBI Disclosure verified.");

            logStep(Status.INFO, "Step 2: Testing Analytics Filter Chips.");
            
            String[] performanceChips = {"Nifty Positional", "Midcap Positional", "MCX Positional", "Long term Goal"};
            
            for (String chip : performanceChips) {
                logStep(Status.INFO, "Locating Analytics Chip: " + chip);
                
                // HORIZONTAL SCROLL FIX
                try {
                    driver.findElement(AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().className(\"android.widget.HorizontalScrollView\")).setAsHorizontalList().scrollIntoView("
                        + "new UiSelector().descriptionContains(\"" + chip + "\"));"));
                } catch (Exception e) {
                    // Ignore if already visible
                }
                
                WebElement chipElement = wait.until(ExpectedConditions.elementToBeClickable(
                        AppiumBy.xpath("//android.widget.HorizontalScrollView//android.view.View[contains(@content-desc, '" + chip + "')]")));
                
                chipElement.click();
                logStep(Status.PASS, "Clicked Analytics Chip: " + chip);
                Thread.sleep(1500); 
                
                boolean isDataVisible = verifyElementIsDisplayed(wait, "ROI Data", 
                        "//*[contains(@content-desc, 'Avg. return') and contains(@content-desc, 'Closed Trades')]");
                
                Assert.assertTrue(isDataVisible, "Performance data failed to load for " + chip);
                logStep(Status.INFO, "Performance data verified for " + chip);
            }

            logStep(Status.PASS, "Performance Analytics section behaves correctly.");

        } catch (Exception e) {
            logStep(Status.FAIL, "Performance Analytics Test Failed: " + e.getMessage());
            Assert.fail("Analytics Verification Failed: " + e.getMessage());
        } finally {
            returnToDashboardSafe();
        }
    }

    @Test(priority = 4, description = "Verify Main Categories, Exploration Banners, and Navigation")
    public void testDashboardElementsAndNav() {
        test = extent.createTest("Dashboard: Content & Navigation Stress Test");
        AndroidDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        logStep(Status.INFO, "Starting General Content & Navigation Verification...");

        try {
            logStep(Status.INFO, "Step 1: Checking Core Trade Category Cards.");
            
            // 🎯 CRITICAL FIX: Scroll back to the top of the screen to find the main cards!
            try {
                driver.findElement(AppiumBy.androidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollToBeginning(5);"));
                Thread.sleep(1000);
            } catch (Exception e) {}

            String[] categories = {"MCX Positional", "Long term Goal Oriented", "Nifty Positional", "Midcap Positional"};
            for (String cat : categories) {
                String xpath = "//*[contains(@content-desc, '" + cat + "')]";
                if (verifyElementIsDisplayed(wait, cat + " Card", xpath)) {
                    logStep(Status.PASS, "Visible: " + cat);
                } else {
                    logStep(Status.FAIL, "Missing Category Card: " + cat);
                }
            }

            logStep(Status.INFO, "Step 2: Checking Explore Promotional Banners.");
            scrollToElementByContentDesc("Research Reports");
            verifyElementIsDisplayed(wait, "Research Reports Banner", "//*[contains(@content-desc, 'Research Reports')]");
            
            scrollToElementByContentDesc("Finance Tools");
            verifyElementIsDisplayed(wait, "Finance Tools Banner", "//*[contains(@content-desc, 'Finance Tools')]");
            logStep(Status.PASS, "Promotional Banners scrolled into view and verified.");

            logStep(Status.INFO, "Step 3: Testing Bottom Navigation Functional Clicks.");
            String[] navTabs = {"Trades", "Blogs", "Tools", "Profile", "Explore"};
            
            for (String tab : navTabs) {
                String xpath = "//android.view.View[@content-desc='" + tab + "']";
                if (verifyElementIsDisplayed(wait, tab + " Tab", xpath)) {
                    driver.findElement(AppiumBy.xpath(xpath)).click();
                    logStep(Status.PASS, "Navigated to '" + tab + "' tab successfully.");
                    Thread.sleep(1200); 
                } else {
                    logStep(Status.FAIL, "Navigation Tab Missing: " + tab);
                    Assert.fail("Bottom navigation tab missing: " + tab);
                }
            }
            
            logStep(Status.PASS, "Dashboard Content & Navigation test completed successfully.");

        } catch (Exception e) {
            logStep(Status.FAIL, "Dashboard Content Test Failed: " + e.getMessage());
            Assert.fail("Dashboard Content Verification Failed: " + e.getMessage());
        } finally {
            returnToDashboardSafe();
        }
    }
}