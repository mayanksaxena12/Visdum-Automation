package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Change-password modal (inline in UsersList.tsx). The password must satisfy the Yup schema:
 * 8-20 chars with at least one uppercase, lowercase, digit, and special character.
 */
public class ChangePasswordModal extends BasePage {

    private final By newPassword = By.name("new_password");
    private final By confirmPassword = By.name("confirm_password");
    private final By update = By.xpath("//button[normalize-space()='Update']");

    public ChangePasswordModal(WebDriver driver) {
        super(driver);
    }

    public void setPassword(String password) {
        type(newPassword, password);
        type(confirmPassword, password);
    }

    public void update() {
        click(update);
    }

    /** True once the modal has closed after a successful update (the Update button unmounts). */
    public boolean isClosed() {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(update));
    }
}
