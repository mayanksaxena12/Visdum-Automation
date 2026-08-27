package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;


import java.util.List;

/**
 * Login page (app/modules/auth/components/Login.tsx).
 *
 * <p>Covers the automatable parts of the login flow: field validation (Yup: required / email
 * format / min-max length), invalid-credential and unregistered-email API errors, and the
 * 2-step verification screen where the OTP is read from the UAT database
 * ({@link utilities.OtpDbReader}) and entered into the six digit boxes.
 */
public class LoginPage extends BasePage {

    private final By email = By.name("email");
    private final By password = By.name("password");
    private final By loginBtn = By.id("dc_sign_in_submit");
    private final By twoFactorHeading = By.xpath("//h1[normalize-space()='2-step verification']");
    private final By otpInput = By.cssSelector("input[inputmode='numeric']");
    private final By verifyBtn = By.xpath("//button[normalize-space()='Verify']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /** Full happy-path field entry + submit (used by every module's BaseTest to reach the app). */
    public void login(String user, String pass) {
        type(email, user);
        type(password, pass);
        click(loginBtn);
    }

    public void enterEmail(String value) {
        type(email, value);
    }

    public void enterPassword(String value) {
        type(password, value);
    }

    public void clickLogin() {
        click(loginBtn);
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(loginBtn)).isDisplayed();
    }

    /** True once valid credentials redirect to the 2-step verification screen. */
    public boolean is2FADisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(twoFactorHeading)).isDisplayed();
    }

    /** Enters the given code into the six OTP digit boxes (one digit per box). */
    public void enterOtp(String code) {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(otpInput));
        List<WebElement> boxes = driver.findElements(otpInput);
        if (boxes.size() < 6) {
            throw new IllegalStateException(
                    "Expected at least 6 OTP digit inputs, found " + boxes.size());
        }
        for (int i = 0; i < 6 && i < code.length(); i++) {
            boxes.get(i).sendKeys(String.valueOf(code.charAt(i)));
        }
    }
 
    public void clickVerify() {
        wait.until(ExpectedConditions.elementToBeClickable(verifyBtn)).click();
    }
 
    /** True once a valid OTP has cleared the 2FA screen (redirected to the dashboard). */
    public boolean isOtpVerified() {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(twoFactorHeading));
    }
 
    /** True while the 2-step verification screen is still shown (bad/empty/partial OTP). */
    public boolean isStillOn2FA() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(twoFactorHeading)).isDisplayed();
    }
 
    /** Pastes a full code into the first OTP box (React OTP inputs auto-distribute on paste). */
    public void pasteOtp(String code) {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(otpInput));
        List<WebElement> boxes = driver.findElements(otpInput);
        if (boxes.isEmpty()) {
            throw new IllegalStateException("No OTP digit inputs found.");
        }
        boxes.get(0).sendKeys(code);
    }
 
    /** True once the six OTP boxes together hold exactly the given code. */
    public boolean isOtpFilled(String code) {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(otpInput));
        List<WebElement> boxes = driver.findElements(otpInput);
        if (boxes.size() < 6) {
            return false;
        }
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            value.append(boxes.get(i).getAttribute("value") == null ? ""
                    : boxes.get(i).getAttribute("value"));
        }
        return value.toString().equals(code);
    }

    /** Inline Yup field error (rendered as {@code <span role='alert'>}). */
    public boolean isFieldErrorVisible(String message) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[@role='alert' and normalize-space()=" + xpathLiteral(message) + "]"))).isDisplayed();
    }

    /** Lenient check for an API error toast by a distinctive substring of its message. */
    public boolean isMessageVisible(String substring) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.)," + xpathLiteral(substring) + ")]"))).isDisplayed();
    }
}
