package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;


import java.util.List;
import java.time.Duration;

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
    private final By twoFactorHeading = By.xpath("//h1[contains(.,'2-step verification')] | //*[@id='kt_sing_in_two_factor_form']");
    private final By otpInput = By.cssSelector("#kt_sing_in_two_factor_form input[type='text'], #kt_sing_in_two_factor_form input:not([type='checkbox']), input[inputmode='numeric']");
    private final By verifyBtn = By.xpath("//button[@id='kt_sing_in_two_factor_submit' or normalize-space()='Verify']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /** The server's error toast when it refuses the login ("Unable To Login / Invalid Credentials"). */
    private final By loginRejected = By.xpath("//*[contains(text(),'Unable To Login') or contains(text(),'Unable to login')"
            + " or contains(text(),'Invalid Credentials') or contains(text(),'Invalid credentials')]");

    /** Set once UAT refuses an account's login: later tests fail at once with the same reason. */
    private static volatile String rejectedUser;
    private static volatile String rejectedMessage;

    /** Full happy-path field entry + submit (used by every module's BaseTest to reach the app). */
    public void login(String user, String pass) {
        if (user != null && user.equals(rejectedUser)) {
            throw new IllegalStateException(rejectedMessage);
        }
        type(email, user);
        type(password, pass);
        click(loginBtn);

        // Wait for one of three outcomes: logged in (form gone), 2-step screen, or the server's error.
        String outcome;
        try {
            outcome = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
                if (d.findElements(loginRejected).stream().anyMatch(WebElement::isDisplayed)) {
                    return "rejected";
                }
                // The e-mail field, not the button: the app reuses the button id "dc_sign_in_submit" elsewhere.
                if (d.findElements(email).stream().noneMatch(WebElement::isDisplayed)) {
                    return "left-login-form";
                }
                return null;
            });
        } catch (Exception timeout) {
            outcome = "no-response";
        }
        if ("rejected".equals(outcome)) {
            recordStep("Login rejected by the server");
            rejectedUser = user;
            rejectedMessage = "LOGIN REJECTED: UAT answered \"Unable To Login - Invalid Credentials\" for '" + user
                    + "'. The password in config.properties no longer works for this account, or the account is"
                    + " inactive. This is an account problem, not a test failure; nothing after login was tested."
                    + " Fix the account (or update config.properties) and run again.";
            System.out.println("[AUTH] " + rejectedMessage);
            throw new IllegalStateException(rejectedMessage);
        }
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(twoFactorHeading));
            System.out.println("[AUTH] 2-Step Verification screen detected. Fetching OTP from DB...");
            String otp = utilities.OtpDbReader.latestOtp();
            System.out.println("[AUTH] Latest OTP from DB: " + otp);
            enterOtp(otp);
            clickVerify();
            try {
                new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.invisibilityOfElementLocated(twoFactorHeading));
            } catch (Exception ignored) {
            }
            System.out.println("[AUTH] Logged in successfully.");
        } catch (Exception ignored) {
        }
        recordStep("After login");
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

    /** True once valid credentials redirect to the 2-step verification screen or directly to dashboard. */
    public boolean is2FADisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(twoFactorHeading)).isDisplayed();
        } catch (Exception e) {
            // If 2FA is disabled on UAT, check if we left the login page (login succeeded)
            return driver.findElements(email).isEmpty();
        }
    }

    /** Enters the given code into the six OTP digit boxes (one digit per box). */
    public void enterOtp(String code) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(otpInput));
        List<WebElement> boxes = driver.findElements(otpInput);
        if (boxes.size() < 6) {
            throw new IllegalStateException(
                    "Expected at least 6 OTP digit inputs, found " + boxes.size());
        }
        for (int i = 0; i < 6 && i < code.length(); i++) {
            boxes.get(i).clear();
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

    public boolean isAnyFieldErrorVisible(String... messages) {
        for (String msg : messages) {
            java.util.List<org.openqa.selenium.WebElement> els = driver.findElements(
                    By.xpath("//span[@role='alert' and contains(normalize-space(), " + xpathLiteral(msg) + ")]"));
            if (!els.isEmpty() && els.get(0).isDisplayed()) {
                return true;
            }
        }
        return false;
    }

    /** Lenient check for an API error toast by a distinctive substring of its message. */
    public boolean isMessageVisible(String substring) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.)," + xpathLiteral(substring) + ")]"))).isDisplayed();
    }

    public boolean isAnyMessageVisible(String... substrings) {
        StringBuilder xpath = new StringBuilder("//*[");
        for (int i = 0; i < substrings.length; i++) {
            if (i > 0) xpath.append(" or ");
            xpath.append("contains(normalize-space(.),").append(xpathLiteral(substrings[i])).append(")");
        }
        xpath.append("]");
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath.toString()))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
