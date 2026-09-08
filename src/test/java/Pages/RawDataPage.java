package Pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Raw Data page object (from raw-data/RawDataWrapper.tsx, RawDataListHeader.tsx,
 * and RawDataTable.tsx).
 *
 * <p>Extends {@link AgGridListPage} to leverage AG-Grid row matching, sorting,
 * row actions ellipsis ({@code userAction{index}}), and column headers.
 */
public class RawDataPage extends AgGridListPage {

    private final By headerTitle = By.xpath("//*[normalize-space()='Raw Data']");
    private final By searchInput = By.cssSelector("input[placeholder='Search Here'], input[placeholder='Search']");
    private final By tabsContainer = By.xpath("//ul[contains(@class,'nav-tabs')]");
    private final By activeTab = By.xpath("//ul[contains(@class,'nav-tabs')]//a[contains(@class,'active')]");
    private final By tabLinks = By.xpath("//ul[contains(@class,'nav-tabs')]//a");

    // Export Dropdown
    private final By exportDropdownBtn = By.id("downloadfiles");
    private final By exportDropdownMenu = By.id("dropdown_user");
    private final By exportDataOption = By.xpath("//ul[@id='dropdown_user']//a[contains(.,'Export Data')]");
    private final By exportTemplateOption = By.xpath("//ul[@id='dropdown_user']//a[contains(.,'Export Template')]");

    // Header Kebab Actions
    private final By headerActionsBtn = By.id("rawDataHeaderActions");
    private final By fetchNewDataBtn = By.xpath("//button[contains(.,'Fetch New Data')]");
    private final By uploadDataBtn = By.xpath("//button[contains(.,'Upload Data')]");
    private final By addRecordBtn = By.xpath("//button[contains(.,'Add Record')]");
    private final By funnelFilterBtn = By.xpath("//i[contains(@class,'bi-funnel')]");

    // Confirmation Modal
    private final By modalContainer = By.xpath("//div[contains(@class,'modal-content')]");
    private final By modalPromptText = By.xpath("//div[contains(@class,'modal-body')]//span[contains(@class,'text-dark-400') or contains(text(),'Are you sure')]");
    private final By modalCancelBtn = By.xpath("//div[contains(@class,'modal')]//button[normalize-space()='Cancel']");
    private final By modalSubmitBtn = By.xpath("//div[contains(@class,'modal')]//button[normalize-space()='Submit' or .//span[normalize-space()='Submit']]");

    public RawDataPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(headerTitle)).isDisplayed()
                    || wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput)).isDisplayed();
        } catch (Exception e) {
            return driver.getCurrentUrl().contains("raw-data");
        }
    }

    // --- Data Stream Tabs ---

    public List<String> getDataStreamTabNames() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(tabsContainer));
        return driver.findElements(tabLinks).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    public String getSelectedDataStreamTab() {
        return text(activeTab).trim();
    }

    public void selectDataStreamTab(String streamName) {
        By tab = By.xpath("//ul[contains(@class,'nav-tabs')]//a[normalize-space()=" + xpathLiteral(streamName) + "]");
        click(tab);
    }

    // --- Search ---

    public void search(String keyword) {
        type(searchInput, keyword);
    }

    public void clearSearch() {
        clearField(searchInput);
    }

    // --- Export ---

    public void openExportDropdown() {
        click(exportDropdownBtn);
    }

    public boolean isExportDropdownOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(exportDropdownMenu)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickExportData() {
        click(exportDataOption);
    }

    public void clickExportTemplate() {
        click(exportTemplateOption);
    }

    // --- Header Actions ---

    public boolean isHeaderActionsPresent() {
        return !driver.findElements(headerActionsBtn).isEmpty();
    }

    public void openHeaderActionsMenu() {
        click(headerActionsBtn);
    }

    public boolean isHeaderActionPresent(String actionName) {
        By locator = By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and contains(normalize-space(), " + xpathLiteral(actionName) + ")]"
                + " | //div[contains(@class,'dropdown-menu')]//*[contains(normalize-space(), " + xpathLiteral(actionName) + ")]");
        return !driver.findElements(locator).isEmpty();
    }

    public void clickHeaderAction(String actionName) {
        openHeaderActionsMenu();
        click(By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and contains(normalize-space(), " + xpathLiteral(actionName) + ")]"
                + " | //div[contains(@class,'dropdown-menu')]//*[contains(normalize-space(), " + xpathLiteral(actionName) + ")]"));
    }

    public boolean isFetchNewDataPresent() {
        return !driver.findElements(fetchNewDataBtn).isEmpty();
    }

    public void clickFetchNewData() {
        click(fetchNewDataBtn);
    }

    public boolean isUploadDataPresent() {
        return !driver.findElements(uploadDataBtn).isEmpty();
    }

    // --- Row Actions ---

    public void openRowAction(String identifier, String action) {
        openAction(identifier, action);
    }

    public void openViewDetails(String identifier) {
        openRowAction(identifier, "View Details");
    }

    // --- Confirmation Modal ---

    public boolean isConfirmationModalOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(modalContainer)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getConfirmationModalText() {
        return text(modalPromptText);
    }

    public void cancelModal() {
        click(modalCancelBtn);
    }

    public void confirmModal() {
        click(modalSubmitBtn);
    }
}
