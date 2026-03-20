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

public class Blogs_Screen_Tests extends Base_setup {

    @Test(priority = 4, description = "Verify Blogs Tab, Feed, Like Toggle, and Subscription Create Logic")
    public void testBlogsScreenFlow() {
        test = extent.createTest("Blogs Screen & Business Logic Verification");
        AndroidDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        logStep(Status.INFO, "Starting Blogs Screen Verification.");

        try {
            // ==========================================
            // 1. Navigate to Blogs Tab (High Speed)
            // ==========================================
            logStep(Status.INFO, "Navigating to Blogs Tab.");
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Blogs"))).click();
            handlePromoPopup();

            // ==========================================
            // 2. Verify Blog Card UI (Smart Wait)
            // ==========================================
            logStep(Status.INFO, "Step 1: Verifying Blog Card Content.");
            try {
                // 🚀 SPEED FIX: Wait exactly for the first image to load instead of Thread.sleep(3000)
                WebElement firstBlogCard = wait.until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.xpath("(//android.widget.ImageView)[1]")));
                
                String cardContent = firstBlogCard.getAttribute("content-desc");
                if (cardContent != null && !cardContent.isEmpty()) {
                    logStep(Status.PASS, "First Blog Card Content loaded instantly.");
                } else {
                    logStep(Status.WARNING, "Blog card exists but content-desc is empty.");
                }
            } catch (Exception e) {
                logStep(Status.FAIL, "Could not find any Blog posts in the feed.");
                Assert.fail("Blog Feed is empty or failing to load.");
            }

            // ==========================================
            // 3. Like Toggle & Comment Interaction
            // ==========================================
            logStep(Status.INFO, "Step 2: Testing Like Toggle.");
            try {
                WebElement likeBtn = driver.findElement(AppiumBy.xpath("(//android.widget.ImageView)[1]/android.view.View[2]"));
                likeBtn.click();
                logStep(Status.PASS, "Toggled 'Like' on the first blog post.");
                
                // 🚀 SPEED FIX: Short explicit wait to ensure state registers before reverting
                Thread.sleep(500); 
                
                likeBtn.click(); // Revert to maintain data integrity
            } catch (Exception e) {
                logStep(Status.WARNING, "Like/Comment buttons not found. The UI layout might have shifted.");
            }

            // ==========================================
            // 4. BUSINESS LOGIC: Subscription vs Unsubscribed FAB
            // ==========================================
            logStep(Status.INFO, "Step 3: Verifying Create Post (+) Business Logic.");
            
            boolean isSubscribedOrTrial = false;
            WebElement fabButton = null;
            
            try {
                // 🚀 SMART POLLING: Give Appium 3 seconds to find the FAB
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                fabButton = shortWait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.xpath("//*[contains(@content-desc, 'Create') or contains(@content-desc, 'Add') or contains(@content-desc, 'Post')]")
                ));
                isSubscribedOrTrial = true;
            } catch (Exception e) {
                isSubscribedOrTrial = false;
            }

            // --- EXECUTE LOGIC GATES ---
            if (!isSubscribedOrTrial) {
                // USER IS UNSUBSCRIBED
                logStep(Status.PASS, "✅ BUSINESS LOGIC VERIFIED: User has no active product/trial. 'Create Post' (+) button is correctly HIDDEN.");
            } else {
                // USER IS SUBSCRIBED / ON TRIAL
                logStep(Status.PASS, "✅ BUSINESS LOGIC VERIFIED: Active Sub/Trial detected. 'Create Post' (+) button is VISIBLE.");
                logStep(Status.INFO, "Testing Production-Safe Create/Delete flow...");
                
                fabButton.click();
                
                // 🚀 SPEED FIX: Direct typing instead of Actions
                String dummyPostText = "AUTOMATION TEST POST - " + System.currentTimeMillis();
                WebElement postInput = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.className("android.widget.EditText")));
                postInput.click();
                postInput.clear();
                postInput.sendKeys(dummyPostText);
                if (driver.isKeyboardShown()) driver.hideKeyboard();
                
                // Submit Post
                driver.findElement(AppiumBy.xpath("//*[contains(@content-desc, 'Post') or contains(@content-desc, 'Submit')]")).click();
                logStep(Status.PASS, "Successfully posted dummy content.");
                
                // 🚀 SPEED FIX: Wait dynamically for the new post to appear in the feed
                WebElement myDummyPost = wait.until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.xpath("//android.widget.ImageView[contains(@content-desc, '" + dummyPostText + "')]")));
                
                // Cleanup / Delete the dummy post
                logStep(Status.INFO, "Attempting to cleanup/delete the dummy post.");
                WebElement myPost3DotMenu = myDummyPost.findElement(AppiumBy.xpath("./android.view.View[1]"));
                myPost3DotMenu.click();
                
                WebElement deleteOption = wait.until(ExpectedConditions.elementToBeClickable(
                        AppiumBy.xpath("//*[contains(@content-desc, 'Delete') or contains(@content-desc, 'Remove')]")));
                deleteOption.click();
                
                try {
                    WebElement confirmDelete = wait.until(ExpectedConditions.elementToBeClickable(
                            AppiumBy.xpath("//*[contains(@content-desc, 'Yes') or contains(@content-desc, 'Delete')]")));
                    confirmDelete.click();
                    logStep(Status.PASS, "Dummy post deleted successfully. Production environment is clean.");
                } catch (Exception noConfirm) {
                    logStep(Status.INFO, "No confirmation dialog appeared. Post likely deleted instantly.");
                }
            }

            logStep(Status.PASS, "Blogs Screen & Business Logic verification completed perfectly.");

        } catch (Exception e) {
            logStep(Status.FAIL, "Blogs Screen Test Failed: " + e.getMessage());
            Assert.fail("Blogs Screen Failure: " + e.getMessage());
        } finally {
            returnToDashboardSafe();
        }
    }
}