package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Login page (app/modules/auth/components/Login.tsx).
 *
 * <p>Covers the automatable parts of the login flow: field validation (Yup: required / email
 * format / min-max length), invalid-credential and unregistered-email API errors, and the
 * transition to the 2-step verification screen on valid credentials. The OTP/2FA completion itself
 * is not automatable (requires a real emailed code) and remains manual.
 */
public class LoginPage extends BasePage {

    private final By email = By.name("email");
    private final By password = By.name("password");
    private final By loginBtn = By.id("dc_sign_in_submit");
    private final By twoFactorHeading = By.xpath("//h1[normalize-space()='2-step verification']");

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
