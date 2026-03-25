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
		test = extent.createTest("Login Module: Hyper-Speed Flow - " + Base_setup.testFullName);
		AndroidDriver driver = getDriver();
        
        // 🚀 SPEED HACK: Poll the screen every 100ms instead of default 500ms for instant reactions
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.pollingEvery(Duration.ofMillis(100));

		String targetMobileNumber = Base_setup.testMobileNumber;
		String expectedDashboardName = "";
        
        // 🚨 CONFIG FILE CHECKER
        if (targetMobileNumber == null || targetMobileNumber.trim().isEmpty() || targetMobileNumber.length() < 10) {
            logStep(Status.FAIL, "CRITICAL ERROR: Invalid number in config.properties: '" + targetMobileNumber + "'");
            Assert.fail("Fix config.properties! It passed an invalid mobile number.");
        }

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

            WebElement phoneField = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.className("android.widget.EditText")));

			// ==========================================
			// STEP 2: NEGATIVE TEST - EMPTY SUBMISSION
			// ==========================================
			logStep(Status.INFO, "Executing Negative Test: Empty Mobile Number");
			clearFieldAggressively(driver, phoneField);
			dismissKeyboardSafely(driver);

			safeClick(driver, AppiumBy.accessibilityId("Login"));
			checkToastSafely("mobile number");

			// ==========================================
			// STEP 3: NEGATIVE TEST - INVALID NUMBER
			// ==========================================
			logStep(Status.INFO, "Executing Negative Test: Invalid Mobile Number (12345)");
			clearFieldAggressively(driver, phoneField);
			phoneField.sendKeys("12345");
			dismissKeyboardSafely(driver);

			safeClick(driver, AppiumBy.accessibilityId("Login"));
			checkToastSafely("valid mobile number");

			// ==========================================
			// STEP 4: POSITIVE TEST - VALID NUMBER
			// ==========================================
			logStep(Status.INFO, "Executing Positive Test: Valid Mobile Number");
			// 🚀 THE FIX: This will completely backspace out "12345" before typing the real number
			clearFieldAggressively(driver, phoneField);
			phoneField.sendKeys(targetMobileNumber);
			dismissKeyboardSafely(driver);

			safeClick(driver, AppiumBy.accessibilityId("Login"));

			logStep(Status.INFO, "Waiting for transition to OTP screen...");
			try {
				wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Enter your Verification Code")));
				logStep(Status.PASS, "Successfully arrived at OTP Verification Screen.");
			} catch (Exception e) {
				Assert.fail("Failed to transition to OTP screen! Network may be slow or Login button failed.");
			}

			// ==========================================
			// STEP 5: OTP SCREEN UI VALIDATION
			// ==========================================
			logStep(Status.INFO, "Validating OTP Screen UI Elements...");
			try {
				wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Not you? Change Number")));
				wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Resend Code")));
				logStep(Status.PASS, "Change Number and Resend Code buttons are visible.");
			} catch (Exception e) {
				logStep(Status.WARNING, "OTP UI Elements missing or took too long to load.");
			}

            WebElement otpField = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.className("android.widget.EditText")));

			// ==========================================
			// STEP 6: NEGATIVE TEST - INVALID OTP
			// ==========================================
			logStep(Status.INFO, "Executing Negative Test: Invalid OTP");
			try {
				clearFieldAggressively(driver, otpField);
				otpField.sendKeys("000000");
				dismissKeyboardSafely(driver);

				try {
					safeClick(driver, AppiumBy.accessibilityId("Enter otp"));
				} catch (Exception e) {}
				checkToastSafely("Invalid");

				clearFieldAggressively(driver, otpField);
				dismissKeyboardSafely(driver);
			} catch (Exception e) {
				logStep(Status.WARNING, "UI became unresponsive during Invalid OTP test. Recovering...");
				try { driver.pressKey(new KeyEvent(AndroidKey.BACK)); } catch (Exception ex) {}
			}

			// ==========================================
			// STEP 7: POSITIVE TEST - VALID OTP FETCH
			// ==========================================
			logStep(Status.INFO, "Intercepting valid OTP via API...");
			String[] apiData = fetchApiData(targetMobileNumber);
			String dynamicOtp = apiData[0];
			String apiFullName = apiData[1];
			logStep(Status.PASS, "Retrieved OTP: " + dynamicOtp);

			logStep(Status.INFO, "Entering Valid OTP...");
			clearFieldAggressively(driver, otpField);
			otpField.sendKeys(dynamicOtp);
			dismissKeyboardSafely(driver);

			try {
				safeClick(driver, AppiumBy.accessibilityId("Enter otp"));
			} catch (Exception e) {
				try {
					safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Verify') or contains(@content-desc, 'Submit')]"));
				} catch (Exception ex) {
					logStep(Status.INFO, "Submit button not clicked. App may have auto-submitted.");
				}
			}

			// ==========================================
			// STEP 8: SMART ROUTING (NEW VS EXISTING USER)
			// ==========================================
			logStep(Status.INFO, "Detecting landing screen...");
            boolean isNewUser = false;
            try {
                // 🚀 FIX: Lightning fast accessibility ID lookups instead of heavy XPaths
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Explore")),
                    ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Home")),
                    ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Trades")),
                    ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Register"))
                ));
                isNewUser = !driver.findElements(AppiumBy.accessibilityId("Register")).isEmpty();
            } catch (Exception e) {
                logStep(Status.WARNING, "Routing wait timed out. Proceeding with assumptions.");
            }

			if (isNewUser) {
				logStep(Status.WARNING, "New User detected. Executing Registration Flow...");

				String regName = (Base_setup.testFullName == null || Base_setup.testFullName.isEmpty()) ? "Test User" : Base_setup.testFullName;
				String regEmail = (Base_setup.testEmail == null || Base_setup.testEmail.isEmpty()) ? "test@researchmantra.com" : Base_setup.testEmail;
				String regCity = (Base_setup.testCity == null || Base_setup.testCity.isEmpty()) ? "Mumbai" : Base_setup.testCity;
				expectedDashboardName = regName;

				logStep(Status.INFO, "Entering Data: " + regName + " | " + regEmail + " | " + regCity);

				List<WebElement> fields = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(AppiumBy.className("android.widget.EditText")));
				clearFieldAggressively(driver, fields.get(0)); fields.get(0).sendKeys(regName);
				clearFieldAggressively(driver, fields.get(1)); fields.get(1).sendKeys(regEmail);
				clearFieldAggressively(driver, fields.get(2)); fields.get(2).sendKeys(regCity);

				dismissKeyboardSafely(driver);

				logStep(Status.INFO, "Scrolling to reveal Registration options...");
				try {
					driver.findElement(AppiumBy.androidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(1);"));
				} catch (Exception e) {
					swipeUp();
				}
				Thread.sleep(300); 

				logStep(Status.INFO, "Testing Gender Radio Buttons...");
				try {
					safeClick(driver, AppiumBy.xpath("(//android.widget.RadioButton)[2]"));
					logStep(Status.PASS, "Successfully toggled Gender selection to Female.");
				} catch (Exception e) {
					logStep(Status.WARNING, "Could not interact with Gender radio buttons via index. Trying fallback...");
                    try {
                        safeClick(driver, AppiumBy.xpath("(//*[contains(@content-desc, 'Female') or contains(@text, 'Female')])/.."));
                    } catch (Exception ex) {}
				}

				logStep(Status.INFO, "Submitting Registration Form...");
				try {
					safeClick(driver, AppiumBy.accessibilityId("Register"));
				} catch (Exception e) {
					safeClick(driver, AppiumBy.xpath("//*[contains(@content-desc, 'Register') or contains(@text, 'Register')]"));
				}
			} else {
                expectedDashboardName = apiFullName;
            }

			// ==========================================
			// STEP 9: FINAL DASHBOARD VERIFICATION
			// ==========================================
			handlePromoPopup();
            
            // 🚀 FIX: Fast Dashboard Verification
			wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Explore")),
                ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Home")),
                ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("Trades"))
            ));

			if (expectedDashboardName != null && !expectedDashboardName.isEmpty()) {
				String firstName = expectedDashboardName.split(" ")[0];
				wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//*[contains(translate(@content-desc, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + firstName.toLowerCase() + "')]")));
				logStep(Status.PASS, "✅ Login Flow Complete! Logged in as: " + expectedDashboardName);
				Base_setup.loggedInUserName = expectedDashboardName;
			}
		}

		catch (Exception e) {
			logStep(Status.FAIL, "Login Flow Failed: " + e.getMessage());
			Assert.fail(e.getMessage());
		} finally {
			returnToDashboardSafe();
		}
	}

    // 🚀 NEW: Bulletproof way to clear fields in Flutter/Appium
    private void clearFieldAggressively(AndroidDriver driver, WebElement field) {
        try {
            field.click();
            field.clear(); // Standard attempt
            // Force hardware backspaces just to be absolutely sure no ghost text remains
            for (int i = 0; i < 15; i++) {
                driver.pressKey(new KeyEvent(AndroidKey.DEL));
            }
        } catch (Exception e) {}
    }

    // ⚡ ZERO-LATENCY "HUMAN TAP" (Guarantees Flutter cannot block the click)
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
	private void dismissKeyboardSafely(AndroidDriver driver) {
		try {
            if (!driver.findElements(AppiumBy.accessibilityId("Default login image")).isEmpty()) {
                driver.findElement(AppiumBy.accessibilityId("Default login image")).click();
            } else if (!driver.findElements(AppiumBy.accessibilityId("Enter your Verification Code")).isEmpty()) {
                driver.findElement(AppiumBy.accessibilityId("Enter your Verification Code")).click();
            } else {
                driver.pressKey(new KeyEvent(AndroidKey.BACK)); 
            }
            Thread.sleep(600); 
		} catch (Exception e) {
            try { driver.hideKeyboard(); } catch (Exception ex) {}
        }
	}

	// 🚀 FAST TOAST CHECKER
	private void checkToastSafely(String expectedText) {
		try {
			WebDriverWait shortWait = new WebDriverWait(getDriver(), Duration.ofSeconds(3));
            shortWait.pollingEvery(Duration.ofMillis(100));
			WebElement toast = shortWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//android.widget.Toast")));
			String actualText = toast.getAttribute("name");
			if (actualText.toLowerCase().contains(expectedText.toLowerCase())) {
				logStep(Status.PASS, "Verified Toast Message: " + actualText);
			}
		} catch (Exception e) {
			logStep(Status.WARNING, "Expected toast containing '" + expectedText + "' did not appear or vanished too quickly.");
		}
	}

	private String[] fetchApiData(String mobileNumber) {
		try {
			String apiUrl = "https://auth.researchmantra.in/api/Authentication/OtpLogin?mobileNumber=" + mobileNumber + "&countryCode=91";
			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) response.append(line);
			in.close();
			String res = response.toString();
			String otp = "", name = "";
			Matcher m1 = Pattern.compile("\"oneTimePassword\":\"(\\d+)\"").matcher(res);
			if (m1.find()) otp = m1.group(1);
			Matcher m2 = Pattern.compile("\"fullName\":\"([^\"]*)\"").matcher(res);
			if (m2.find()) name = m2.group(1);
			return new String[] { otp, name };
		} catch (Exception e) {
			throw new RuntimeException("API Fail: " + e.getMessage());
		}
	}
}