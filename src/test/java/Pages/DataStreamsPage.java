package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Data Streams page object (from data-streams/DataStreamsWrapper.tsx and StreamTable.tsx).
 *
 * <p>Extends {@link AgGridListPage} to leverage AG-Grid row index resolution, ellipsis menu
 * actions ({@code userAction{index}}), inline "View" button, sorting, and status badges.
 */
public class DataStreamsPage extends AgGridListPage {

    private final By headerTitle = By.xpath("//*[normalize-space()='Data Streams']");
    private final By searchInput = By.cssSelector("input[placeholder='Search Here'], input[placeholder='Search']");
    private final By createStreamBtn = By.xpath("//a[contains(@href,'data-streams/add')] | //button[contains(.,'Create')] | //*[contains(@id,'create_data_stream_toggle')]");
    private final By activeTab = By.xpath("//ul[contains(@class,'nav-tabs')]//a[normalize-space()='Data Streams']");
    private final By draftTab = By.xpath("//ul[contains(@class,'nav-tabs')]//a[normalize-space()='Drafts']");
    private final By activeTabSelected = By.xpath("//ul[contains(@class,'nav-tabs')]//a[normalize-space()='Data Streams' and contains(@class,'active')]");
    private final By draftTabSelected = By.xpath("//ul[contains(@class,'nav-tabs')]//a[normalize-space()='Drafts' and contains(@class,'active')]");

    // Modal dialogs from StreamTable.tsx
    private final By statusChangeModalHeader = By.xpath("//div[contains(@class,'modal')]//span[contains(text(),'Are you sure you want to')]");
    private final By statusChangeProceedBtn = By.xpath("//div[contains(@class,'modal')]//button[contains(.,'Proceed')]");
    private final By modalCancelBtn = By.xpath("//div[contains(@class,'modal')]//button[normalize-space()='Cancel']");
    private final By deleteDraftModalHeader = By.xpath("//div[contains(@class,'modal')]//span[contains(text(),'Are you sure you want to delete the Draft?')]");
    private final By deleteDraftConfirmBtn = By.xpath("//div[contains(@class,'modal')]//button[contains(.,'Delete')]");

    public DataStreamsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(headerTitle)).isDisplayed();
        } catch (Exception e) {
            return driver.getCurrentUrl().contains("data-streams");
        }
    }

    public void search(String keyword) {
        type(searchInput, keyword);
    }

    public void clearSearch() {
        clearField(searchInput);
    }

    public void clickCreateDataStream() {
        try {
            click(createStreamBtn);
        } catch (Exception e) {
            driver.get(utilities.ConfigReader.get("url") + "/data/data-streams/add");
        }
    }

    public void switchToActiveTab() {
        click(activeTab);
    }

    public void switchToDraftTab() {
        click(draftTab);
    }

    public boolean isActiveTabSelected() {
        return !driver.findElements(activeTabSelected).isEmpty();
    }

    public boolean isDraftTabSelected() {
        return !driver.findElements(draftTabSelected).isEmpty();
    }

    public boolean isStreamListed(String streamName) {
        return isRowListed(streamName);
    }

    public void openViewStream(String streamName) {
        openView(streamName);
    }

    public void openEditStream(String streamName) {
        openAction(streamName, "Edit");
    }

    public void openDeactivateStream(String streamName) {
        openAction(streamName, "Deactivate");
    }

    public void openActivateStream(String streamName) {
        openAction(streamName, "Activate");
    }

    public void openDeleteDraft(String streamName) {
        openAction(streamName, "Delete");
    }

    public boolean isStatusChangeModalOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(statusChangeModalHeader)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void confirmStatusChange() {
        click(statusChangeProceedBtn);
    }

    public void cancelStatusChange() {
        click(modalCancelBtn);
    }

    public boolean isDeleteDraftModalOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(deleteDraftModalHeader)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void confirmDeleteDraft() {
        click(deleteDraftConfirmBtn);
    }

    public void cancelDeleteDraft() {
        click(modalCancelBtn);
    }
}
