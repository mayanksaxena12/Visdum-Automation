package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Change-password modal (inline in UsersList.tsx). The password must satisfy the Yup schema:
 * 8-20 chars with at least one uppercase, lowercase, digit, and special character.
 */
public class ChangePasswordModal extends BasePage {

    private final By newPassword = By.name("new_password");
    private final By confirmPassword = By.name("confirm_password");
    private final By autoGenerateBtn = By.xpath("//button[normalize-space()='Auto - Generate Password']");
    private final By sendAlertEmailCheckbox = By.name("send_alert_email");
    private final By update = By.xpath("//button[normalize-space()='Update']");

    public ChangePasswordModal(WebDriver driver) {
        super(driver);
    }

    public void setPassword(String password) {
        type(newPassword, password);
        type(confirmPassword, password);
    }

    public void setPassword(String password, boolean sendAlertEmail) {
        type(newPassword, password);
        type(confirmPassword, password);
        setSendAlertEmail(sendAlertEmail);
    }

    public void clickAutoGeneratePassword() {
        click(autoGenerateBtn);
    }

    public String getNewPasswordValue() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(newPassword)).getAttribute("value");
    }

    public void setSendAlertEmail(boolean check) {
        WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(sendAlertEmailCheckbox));
        if (checkbox.isSelected() != check) {
            checkbox.click();
        }
    }

    public void update() {
        click(update);
    }

    /** True once the modal has closed after a successful update (the Update button unmounts). */
    public boolean isClosed() {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(update));
    }

    public void cancel() {
        By cancelBtn = By.xpath("//div[contains(@class,'modal')]//button[normalize-space()='Cancel']");
        click(cancelBtn);
    }

    public boolean isOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(newPassword)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ---- helpers for KM-009..KM-013 ----
    private final By alertCheckbox = By.name("send_alert_email");

    /** Types into New/Confirm password and clicks Update (validation runs; nothing saved if invalid). */
    public void tryUpdate(String newPwd, String confirmPwd) {
        type(newPassword, newPwd);
        type(confirmPassword, confirmPwd);
        click(update);
    }

    public boolean waitForError(String message) {
        boolean shown = anyVisible(By.xpath("//span[@role='alert' and contains(normalize-space()," + xpathLiteral(message) + ")]"));
        recordStep("Verify message '" + message + "' shown: " + shown);
        return shown;
    }

    public boolean isAlertCheckboxVisible() {
        return anyVisible(By.xpath("//label[normalize-space()='Send alert email to new user about their account']"));
    }

    public boolean isAlertChecked() {
        return firstVisible(alertCheckbox).isSelected();
    }

    public void toggleAlert() {
        firstVisible(alertCheckbox).click();
        recordStep("Toggled 'Send alert email'");
    }

    /**
     * True if the popup is still open after up to 5s. The modal fades out, so checking straight after
     * Cancel/Update still finds the fields for a moment; wait for them to go before deciding.
     */
    public boolean isModalStillOpen() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.invisibilityOfElementLocated(newPassword));
            return false;
        } catch (org.openqa.selenium.TimeoutException stillShown) {
            return true;
        }
    }

}


