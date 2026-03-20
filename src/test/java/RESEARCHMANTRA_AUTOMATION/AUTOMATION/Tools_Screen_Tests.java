package RESEARCHMANTRA_AUTOMATION.AUTOMATION;

import java.time.Duration;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.aventstack.extentreports.Status;
import io.appium.java_client.AppiumBy;

public class Tools_Screen_Tests extends Base_setup {

    @Test(priority = 5, description = "Verify Tools Tab UI and Open All Calculators (TC-075 to TC-085)")
    public void testToolsScreenFlow() {
        test = extent.createTest("Tools & Calculators Verification - User: " + Base_setup.testFullName);
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));

        logStep(Status.INFO, "Starting Tools Screen Verification.");

        String[] calculators = {
            "Retirement Plans",
            "SIP Calculator",
            "Risk Reward Calculator",
            "Car Loan Full payment",
            "GST Savings calculator",
            "Home Loan Repayment",
            "Sukanya samriddhi Yojana",
            "Lumpsum Calculator",
            "CAGR Calculator",
            "Salary Budget Calculator"
        };

        try {
            // ==========================================
            // 1. Navigate to Tools Tab (High Speed)
            // ==========================================
            logStep(Status.INFO, "Navigating to Tools Tab.");
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Tools"))).click();
            
            // 🚀 SMART WAIT: Wait exactly for the first calculator to render instead of sleeping
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Retirement Plans")));
            logStep(Status.PASS, "Successfully opened Tools Screen instantly.");

            // ==========================================
            // 2. Iterate, Open, and Close each Calculator
            // ==========================================
            logStep(Status.INFO, "Step 2: High-Speed Navigation Test for " + calculators.length + " Financial Tools.");

            for (String calcName : calculators) {
                try {
                    scrollToElementByContentDesc(calcName);
                    
                    WebElement calcCard = wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId(calcName)));
                    calcCard.click();
                    
                    // 🚀 SMART WAIT: Since every calculator has input boxes, wait for an EditText to prove it loaded!
                    wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.className("android.widget.EditText")));
                    logStep(Status.INFO, "Opened: " + calcName);
                    
                    // TODO: Implement Dummy Data Entry and Calculation validations here in the future
                    
                    navigateBack();
                    
                    // 🚀 SMART WAIT: Verify we are back on the main Tools menu instantly before looping
                    wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId(calcName)));
                    logStep(Status.PASS, "Successfully verified navigation for: " + calcName);
                    
                } catch (Exception e) {
                    logStep(Status.FAIL, "Failed to interact with " + calcName + ". Error: " + e.getMessage());
                    try { navigateBack(); } catch (Exception ex) {} // Safe recovery
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
    
    private void navigateBack() {
        try {
            getDriver().navigate().back();
        } catch (Exception e) {
            try {
                getDriver().findElement(AppiumBy.className("android.widget.Button")).click();
            } catch (Exception ex) {}
        }
    }
}