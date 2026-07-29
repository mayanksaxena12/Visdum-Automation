package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Read-only user drawer from UserActionModals/UserView.tsx.
 *
 * <p>Each field is an {@code <li>} holding two spans: a label span ({@code fs-6 text-gray-500}) and
 * a value span ({@code fs-6} without the muted class). We locate the value by finding the list item
 * whose label matches, then selecting the sibling {@code fs-6} span that is not the muted label.
 */
public class UserViewPage extends BasePage {

    private final By backButton = By.xpath("//button[normalize-space()='Back']");

    public UserViewPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(backButton)).isDisplayed();
    }

    private String value(String label) {
        return text(By.xpath("//li[.//span[normalize-space()=" + xpathLiteral(label) + "]]"
                + "//span[contains(@class,'fs-6') and not(contains(@class,'text-gray-500'))]"));
    }

    public String getName() {
        return value("Name");
    }

    public String getEmail() {
        return value("E-mail");
    }

    public String getReferenceId() {
        return value("User Reference ID");
    }

    public String getRole() {
        return value("Role");
    }

    public String getCurrency() {
        return value("Currency");
    }

    public String getEmployeeId() {
        return value("Employee Id");
    }

    public void close() {
        click(backButton);
    }
}
