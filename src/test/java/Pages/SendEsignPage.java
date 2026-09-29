package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the Send E-Sign Envelopes stepper page (/plans/e-sign/send-esign).
 * Handles envelope selection from the AG-Grid and sending envelopes in bulk.
 */
public class SendEsignPage extends BasePage {

    private final By sendBtn = By.xpath("//button[normalize-space()='Send' or (contains(@class,'btn-primary') and contains(.,'Send'))]");
    private final By selectAllCheckbox = By.xpath("//div[contains(@class,'ag-header-select-all')]");
    private final By rowCheckboxes = By.xpath("//div[@role='row' and @row-index]//div[contains(@class,'ag-selection-checkbox')]");
    private final By cancelBtn = By.xpath("//button[normalize-space()='Cancel' or contains(.,'Cancel')]");

    public SendEsignPage(WebDriver driver) {
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

    public void clickSend() {
        click(sendBtn);
    }

    public void clickCancel() {
        click(cancelBtn);
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.urlContains("/plans/e-sign/send-esign"));
    }
}
