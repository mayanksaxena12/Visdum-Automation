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
}

