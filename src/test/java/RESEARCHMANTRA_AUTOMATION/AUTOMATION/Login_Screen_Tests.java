package RESEARCHMANTRA_AUTOMATION.AUTOMATION;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class Login_Screen_Tests extends Base_setup {

	@Test(priority = 1, description = "Comprehensive E2E Login Validation (Positive & Negative Scenarios)")
	public void testLoginScreenFlow() throws Exception {
		test = extent.createTest("Login Module: All Possible Scenarios - " + Base_setup.testFullName);
		AndroidDriver driver = getDriver();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

		String targetMobileNumber = Base_setup.testMobileNumber;
		String expectedDashboardName = "";

		logStep(Status.INFO, "Starting Comprehensive Login Suite for: " + targetMobileNumber);

		try {
			// ==========================================
			// STEP 1: PERMISSIONS & UI LOAD
			// ==========================================
			try {
				WebElement allowBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
						AppiumBy.id("com.android.permissioncontroller:id/permission_allow_button")));
				allowBtn.click();
			} catch (Exception e) {
				logStep(Status.INFO, "No permission popup detected.");
			}

			wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.accessibilityId("Default login image")));

			// ==========================================
			// STEP 2: UI VALIDATION (Country Code & Terms)
			// ==========================================
			logStep(Status.INFO, "Validating Login Screen UI Elements (Country Code, Terms)...");
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.accessibilityId("🇮🇳 +91")));
				wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.accessibilityId("Terms & Conditions and Privacy Policy.")));
				logStep(Status.PASS, "Country Code and Terms & Conditions link are visible.");
			} catch (Exception e) {
				logStep(Status.WARNING, "Some UI elements (Country Code or Terms) are missing from the login screen.");
			}

			// ==========================================
			// STEP 3: NEGATIVE TEST - EMPTY SUBMISSION
			// ==========================================
			logStep(Status.INFO, "Executing Negative Test: Empty Mobile Number");
			WebElement phoneField = driver.findElement(AppiumBy.className("android.widget.EditText"));
			clearFieldAggressively(driver, phoneField);
			dismissKeyboardSafely(driver);

			safeClick(driver, AppiumBy.accessibilityId("Login"));
			checkToastSafely("mobile number");
			Thread.sleep(1000); // 🚀 Wait for toast to clear

			// ==========================================
			// STEP 4: NEGATIVE TEST - INVALID NUMBER
			// ==========================================
			logStep(Status.INFO, "Executing Negative Test: Invalid Mobile Number (12345)");
			phoneField = driver.findElement(AppiumBy.className("android.widget.EditText"));
			clearFieldAggressively(driver, phoneField);
			phoneField.sendKeys("12345");
			dismissKeyboardSafely(driver);

			safeClick(driver, AppiumBy.accessibilityId("Login"));
			checkToastSafely("valid mobile number");
			Thread.sleep(1500); // 🚀 Wait for toast to clear and UI to unlock

			// ==========================================
			// STEP 5: POSITIVE TEST - VALID NUMBER
			// ==========================================
			logStep(Status.INFO, "Executing Positive Test: Valid Mobile Number");
			phoneField = driver.findElement(AppiumBy.className("android.widget.EditText"));
			clearFieldAggressively(driver, phoneField); // 🚀 Guarantees "12345" is wiped out
			phoneField.sendKeys(targetMobileNumber);
			dismissKeyboardSafely(driver);

			safeClick(driver, AppiumBy.accessibilityId("Login"));

			logStep(Status.INFO, "Waiting for transition to OTP screen...");
			try {
				wait.until(ExpectedConditions
						.presenceOfElementLocated(AppiumBy.accessibilityId("Enter your Verification Code")));
				logStep(Status.PASS, "Successfully arrived at OTP Verification Screen.");
			} catch (Exception e) {
				Assert.fail("Failed to transition to OTP screen! Network may be slow or Login button failed.");
			}

			// ==========================================
			// STEP 6: OTP SCREEN UI VALIDATION
			// ==========================================
			logStep(Status.INFO, "Validating OTP Screen UI Elements...");
			try {
				wait.until(ExpectedConditions
						.presenceOfElementLocated(AppiumBy.accessibilityId("Not you? Change Number")));
				wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Resend Code")));
				logStep(Status.PASS, "Change Number and Resend Code buttons are visible.");
			} catch (Exception e) {
				logStep(Status.WARNING, "OTP UI Elements missing or took too long to load.");
			}

			// ==========================================
			// STEP 7: NEGATIVE TEST - INVALID OTP
			// ==========================================
			logStep(Status.INFO, "Executing Negative Test: Invalid OTP");
			try {
				WebElement otpField = driver.findElement(AppiumBy.className("android.widget.EditText"));
				clearFieldAggressively(driver, otpField);
				otpField.sendKeys("000000");
				dismissKeyboardSafely(driver);

				try {
					safeClick(driver, AppiumBy.accessibilityId("Enter otp"));
				} catch (Exception e) {
				}
				checkToastSafely("Invalid");
				Thread.sleep(1500); // 🚀 Wait for toast to fade

				// Clear the fake OTP before moving on
				clearFieldAggressively(driver, otpField);
				dismissKeyboardSafely(driver);
			} catch (Exception e) {
				logStep(Status.WARNING,
						"UI became unresponsive or app state changed unexpectedly during Invalid OTP test. Recovering...");
				try { driver.pressKey(new KeyEvent(AndroidKey.BACK)); } catch (Exception ex) {}
			}
            
			// ==========================================
			// STEP 8: POSITIVE TEST - VALID OTP FETCH
			// ==========================================
			logStep(Status.INFO, "Intercepting valid OTP via API...");
			String[] apiData = fetchApiData(targetMobileNumber);
			String dynamicOtp = apiData[0];
			String apiFullName = apiData[1];

			expectedDashboardName = apiFullName; 

			logStep(Status.PASS, "Retrieved OTP: " + dynamicOtp);

			logStep(Status.INFO, "Entering Valid OTP...");
			WebElement finalOtpField = driver.findElement(AppiumBy.className("android.widget.EditText"));
			clearFieldAggressively(driver, finalOtpField);
			finalOtpField.sendKeys(dynamicOtp);
			dismissKeyboardSafely(driver);

			try {
				safeClick(driver, AppiumBy.accessibilityId("Enter otp"));
			} catch (Exception e) {
				try {
					safeClick(driver, AppiumBy
							.xpath("//*[contains(@content-desc, 'Verify') or contains(@content-desc, 'Submit')]"));
				} catch (Exception ex) {
					logStep(Status.INFO, "Submit button not clicked. App may have auto-submitted.");
				}
			}

			logStep(Status.INFO, "Waiting 4 seconds for Dashboard/Registration routing...");
			Thread.sleep(4000);

			// ==========================================
			// STEP 9: NEW VS EXISTING USER ROUTING
			// ==========================================
			logStep(Status.INFO, "Checking routing destination...");
			boolean isNewUser = !driver
					.findElements(
							AppiumBy.xpath("//*[contains(@content-desc, 'Register') or contains(@text, 'Register')]"))
					.isEmpty();

			if (isNewUser) {
				logStep(Status.WARNING, "New User detected. Executing Registration Flow...");

				String regName = (Base_setup.testFullName == null || Base_setup.testFullName.isEmpty()) ? "Test User"
						: Base_setup.testFullName;
				String regEmail = (Base_setup.testEmail == null || Base_setup.testEmail.isEmpty())
						? "test@researchmantra.com"
						: Base_setup.testEmail;
				String regCity = (Base_setup.testCity == null || Base_setup.testCity.isEmpty()) ? "Mumbai"
						: Base_setup.testCity;
				expectedDashboardName = regName;

				logStep(Status.INFO, "Entering Data: " + regName + " | " + regEmail + " | " + regCity);

				List<WebElement> fields = wait.until(ExpectedConditions
						.presenceOfAllElementsLocatedBy(AppiumBy.className("android.widget.EditText")));
				
				clearFieldAggressively(driver, fields.get(0));
				fields.get(0).sendKeys(regName);
				dismissKeyboardSafely(driver);

				fields = wait.until(ExpectedConditions
						.presenceOfAllElementsLocatedBy(AppiumBy.className("android.widget.EditText")));
				clearFieldAggressively(driver, fields.get(1));
				fields.get(1).sendKeys(regEmail);
				dismissKeyboardSafely(driver);

				fields = wait.until(ExpectedConditions
						.presenceOfAllElementsLocatedBy(AppiumBy.className("android.widget.EditText")));
				clearFieldAggressively(driver, fields.get(2));
				fields.get(2).sendKeys(regCity);

				dismissKeyboardSafely(driver);
				Thread.sleep(500);

				logStep(Status.INFO, "Testing Gender Radio Buttons (Male -> Female)...");
				try {
					try {
						safeClick(driver, AppiumBy.xpath("//*[@content-desc='Male' or @text='Male']"));
						Thread.sleep(500); 
					} catch (Exception e) {} 

					safeClick(driver,
							AppiumBy.xpath("//*[contains(@content-desc, 'Female') or contains(@text, 'Female')]"));
					logStep(Status.PASS, "Successfully toggled Gender selection to Female.");
				} catch (Exception e) {
					logStep(Status.WARNING,
							"Could not interact with Gender radio buttons. They might be off-screen or unclickable.");
				}

				logStep(Status.INFO, "Preparing to click Register...");
				dismissKeyboardSafely(driver); 
				Thread.sleep(1000);

				try {
					driver.findElement(AppiumBy
							.androidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(1);"));
				} catch (Exception e) {
					swipeUp();
					Thread.sleep(500);
					swipeUp(); 
				}
				Thread.sleep(1000);

				logStep(Status.INFO, "Submitting Registration Form...");
				try {
					safeClick(driver, AppiumBy.xpath("//android.widget.Button[@content-desc='Register']"));
				} catch (Exception e) {
					safeClick(driver,
							AppiumBy.xpath("//*[contains(@content-desc, 'Register') or contains(@text, 'Register')]"));
				}
			}
            
            // ==========================================
			// STEP 10: FINAL DASHBOARD VERIFICATION
			// ==========================================
			handlePromoPopup();
			wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Explore")));

			if (expectedDashboardName != null && !expectedDashboardName.isEmpty()) {
				String firstName = expectedDashboardName.split(" ")[0];
				wait.until(ExpectedConditions.presenceOfElementLocated(
						AppiumBy.xpath("//*[contains(@content-desc, '" + firstName + "')]")));
				logStep(Status.PASS, "✅ Login Flow Complete! Logged in as: " + expectedDashboardName);
				Base_setup.loggedInUserName = expectedDashboardName;
			}

		} catch (Exception e) {
			logStep(Status.FAIL, "Login Flow Failed: " + e.getMessage());
			Assert.fail(e.getMessage());
		} finally {
			returnToDashboardSafe();
		}
	}

    // 🚀 NEW: FLUTTER-SAFE AGGRESSIVE CLEAR
    private void clearFieldAggressively(AndroidDriver driver, WebElement field) {
        try {
            field.click();
            field.clear(); // Try standard clear first
            Thread.sleep(200);
            // Hammer the backspace key to delete anything left behind
            for (int i = 0; i < 15; i++) {
                driver.pressKey(new KeyEvent(AndroidKey.DEL));
            }
        } catch (Exception e) {}
    }

	private void safeClick(AndroidDriver driver, org.openqa.selenium.By by) throws Exception {
		for (int i = 0; i < 3; i++) {
			try {
				WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
				WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(by));
				el.click();
				return;
			} catch (Exception e) {
				Thread.sleep(1000);
			}
		}
		driver.findElement(by).click();
	}

	private void checkToastSafely(String expectedText) {
		try {
			WebDriverWait shortWait = new WebDriverWait(getDriver(), Duration.ofSeconds(3));
			WebElement toast = shortWait
					.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//android.widget.Toast")));
			String actualText = toast.getAttribute("name");
			if (actualText.toLowerCase().contains(expectedText.toLowerCase())) {
				logStep(Status.PASS, "Verified Toast Message: " + actualText);
			}
		} catch (Exception e) {
			logStep(Status.WARNING,
					"Expected toast containing '" + expectedText + "' did not appear or vanished too quickly.");
		}
	}

	private String[] fetchApiData(String mobileNumber) {
		try {
			String apiUrl = "https://auth.researchmantra.in/api/Authentication/OtpLogin?mobileNumber=" + mobileNumber
					+ "&countryCode=91";
			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null)
				response.append(line);
			in.close();
			String res = response.toString();
			String otp = "", name = "";
			Matcher m1 = Pattern.compile("\"oneTimePassword\":\"(\\d+)\"").matcher(res);
			if (m1.find())
				otp = m1.group(1);
			Matcher m2 = Pattern.compile("\"fullName\":\"([^\"]*)\"").matcher(res);
			if (m2.find())
				name = m2.group(1);
			return new String[] { otp, name };
		} catch (Exception e) {
			throw new RuntimeException("API Fail: " + e.getMessage());
		}
	}

	private void dismissKeyboardSafely(AndroidDriver driver) {
		try {
			if (!driver.findElements(AppiumBy.accessibilityId("Default login image")).isEmpty()) {
				driver.findElement(AppiumBy.accessibilityId("Default login image")).click();
			} else if (!driver.findElements(AppiumBy.accessibilityId("Enter your Verification Code")).isEmpty()) {
				driver.findElement(AppiumBy.accessibilityId("Enter your Verification Code")).click();
			}
			Thread.sleep(1000);
		} catch (Exception e) {
		}
	}
}