package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Deactivate-user confirmation modal (inline in UsersList.tsx).
 *
 * <p>The last-working-day picker is a Flatpickr input ({@code name='payout_date'}) with
 * {@code allowInput}, so we can type the date directly and confirm with Enter to close the calendar.
 * The Submit button ({@link CustomButtonForApiCalling}) stays disabled until a date is selected.
 */
public class DeactivateUserModal extends BasePage {

    private final By lastWorkingDay = By.cssSelector("input[name='payout_date']");
    private final By submit = By.xpath("//button[normalize-space()='Submit']");

    public DeactivateUserModal(WebDriver driver) {
        super(driver);
    }

    public void setLastWorkingDay(String isoDate) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(lastWorkingDay));
        input.sendKeys(isoDate);
        input.sendKeys(Keys.ENTER);
    }

    public void submit() {
        click(submit);
    }
}
