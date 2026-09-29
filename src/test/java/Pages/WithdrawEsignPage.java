package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the Withdraw E-Sign Envelopes page (/plans/e-sign/withdraw-esign).
 * Handles selecting sent envelopes and triggering bulk withdrawal.
 */
public class WithdrawEsignPage extends BasePage {

    private final By withdrawBtn = By.xpath("//button[normalize-space()='Withdraw' or (contains(@class,'btn-primary') and contains(.,'Withdraw'))]");
    private final By selectAllCheckbox = By.xpath("//div[contains(@class,'ag-header-select-all')]");
    private final By rowCheckboxes = By.xpath("//div[@role='row' and @row-index]//div[contains(@class,'ag-selection-checkbox')]");
    private final By cancelBtn = By.xpath("//button[normalize-space()='Cancel' or contains(.,'Cancel')]");

    public WithdrawEsignPage(WebDriver driver) {
        super(driver);
    }

    public void selectFirstEnvelope() {
        wait.until(ExpectedConditions.presenceOfElementLocated(rowCheckboxes));
        List<WebElement> boxes = driver.findElements(rowCheckboxes);
        if (!boxes.isEmpty()) {
            boxes.get(0).click();
        }
    }

    public void selectAllEnvelopes() {
        wait.until(ExpectedConditions.elementToBeClickable(selectAllCheckbox)).click();
    }

    public void clickWithdraw() {
        click(withdrawBtn);
    }

    public void clickCancel() {
        click(cancelBtn);
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.urlContains("/plans/e-sign/withdraw-esign"));
    }
}
