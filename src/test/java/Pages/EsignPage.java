package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the E-Sign dashboard and envelope tracking list (/plans/e-sign).
 * Handles header envelope operations (Send, Withdraw), template mappings,
 * and the Compose Email modal.
 */
public class EsignPage extends AgGridListPage {

    // Header Action Buttons
    private final By sendEnvelopeBtn = By.xpath("//button[contains(.,'Send Envelope')]");
    private final By withdrawEnvelopeBtn = By.xpath("//button[contains(.,'Withdraw Envelope')]");
    private final By headerEllipsisMenu = By.id("esignHeaderActions");
    private final By composeEmailOption = By.xpath("//*[contains(@class,'menu-link') or contains(@class,'menu-item')][contains(.,'Compose Email')]");
    private final By templateMappingOption = By.xpath("//*[contains(@class,'menu-link') or contains(@class,'menu-item')][contains(.,'Template Mapping')]");

    // Compose Email Modal
    private final By composeEmailModal = By.xpath("//div[contains(@class,'modal-title') and contains(.,'Compose Email')]");
    private final By emailTitleInput = By.name("title");
    private final By emailBodyEditor = By.xpath("//div[contains(@class,'ql-editor') or @contenteditable='true']");
    private final By restoreEmailBtn = By.xpath("//span[contains(.,'Restore')]");
    private final By closeModalBtn = By.xpath("//button[contains(@class,'btn-close')]");

    // Search bar
    private final By searchInput = By.xpath("//input[contains(@placeholder,'Search') or @data-dc-search='true']");

    public EsignPage(WebDriver driver) {
        super(driver);
    }

    public void clickSendEnvelope() {
        click(sendEnvelopeBtn);
    }

    public void clickWithdrawEnvelope() {
        click(withdrawEnvelopeBtn);
    }

    public void openComposeEmailModal() {
        click(headerEllipsisMenu);
        click(composeEmailOption);
        wait.until(ExpectedConditions.visibilityOfElementLocated(composeEmailModal));
    }

    public void enterEmailTitle(String title) {
        type(emailTitleInput, title);
    }

    public void clickRestoreEmailTemplate() {
        click(restoreEmailBtn);
    }

    public void closeComposeEmailModal() {
        click(closeModalBtn);
    }

    public void searchEnvelope(String query) {
        type(searchInput, query);
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.urlContains("/plans/e-sign"));
    }
}
