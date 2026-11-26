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
 * If the user has an assigned plan, a Process Payout radio choice (Yes/No) is rendered.
 * The Submit button stays disabled until a date is selected.
 */
public class DeactivateUserModal extends BasePage {

    private final By lastWorkingDay = By.cssSelector("input[name='payout_date']");
    private final By processPayoutYes = By.xpath("//input[@type='radio' and @value='Yes']");
    private final By processPayoutNo = By.xpath("//input[@type='radio' and @value='No']");
    private final By submit = By.xpath("//button[normalize-space()='Submit']");

    public DeactivateUserModal(WebDriver driver) {
        super(driver);
    }

    public void setLastWorkingDay(String isoDate) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(lastWorkingDay));
        input.clear();
        input.sendKeys(isoDate);
        input.sendKeys(Keys.ENTER);
    }

    /** Selects whether payouts should continue to be processed after the last working day. */
    public void selectProcessPayout(boolean process) {
        if (!driver.findElements(processPayoutYes).isEmpty()) {
            By targetRadio = process ? processPayoutYes : processPayoutNo;
            click(targetRadio);
        }
    }

    public void submit() {
        click(submit);
    }

    public void cancel() {
        By cancelBtn = By.xpath("//div[contains(@class,'modal')]//button[normalize-space()='Cancel']");
        click(cancelBtn);
    }

    public boolean isOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(lastWorkingDay)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ---- helpers for KM-014..KM-019 ----
    public boolean isSubmitEnabled() {
        return firstVisible(submit).isEnabled();
    }

    public String lastWorkingDayValue() {
        return firstVisible(lastWorkingDay).getAttribute("value");
    }

    /** Like {@link #isPayoutQuestionVisible()} but gives up after {@code seconds} (for probing several users). */
    public boolean hasPayoutQuestion(int seconds) {
        try {
            return new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(seconds)).until(d ->
                    d.findElements(By.xpath("//label[contains(normalize-space(),'Do you want payouts to be processed')]"))
                            .stream().anyMatch(org.openqa.selenium.WebElement::isDisplayed));
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public boolean isPayoutQuestionVisible() {
        return anyVisible(By.xpath("//label[contains(normalize-space(),'Do you want payouts to be processed')]"));
    }

    public boolean isPayoutSelected(boolean yes) {
        return firstVisible(yes ? processPayoutYes : processPayoutNo).isSelected();
    }

    public void clickPayout(boolean yes) {
        firstVisible(yes ? processPayoutYes : processPayoutNo).click();
        recordStep("Chose payout " + (yes ? "Yes" : "No"));
    }

    /** True once the modal has closed (date input gone). */
    public boolean isGone() {
        try {
            return wait.until(org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated(lastWorkingDay));
        } catch (Exception e) {
            return false;
        }
    }

}


