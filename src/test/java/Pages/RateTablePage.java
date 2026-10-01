package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the Rate Table list and view page (/plans/rate-table).
 *
 * <p>Backed by frontend component {@code RateTableWrapper.tsx} and {@code RateTableListHeader.tsx}.
 * Manages:
 * - Rate Table tabs / header list (switching between rate tables)
 * - Create Rate Table navigation button
 * - In-table operations (Add Row, Upload File, Download)
 * - Rate Table actions (Edit Rate Table -> /rate-table/edit, Delete Rate Table)
 * - Search and grid interactions
 */
public class RateTablePage extends AgGridListPage {

    // Header Create Rate Table button
    private final By createRateTableBtn = By.xpath(
            "//button[contains(@id,'ADD_UPDATE_BUTTON') or contains(.,'Create Rate Table') or contains(.,'Add Create Rate Table')]"
            + " | //a[contains(@href,'/rate-table/add')]");

    // Search bar (ListSearchComponent)
    private final By searchInput = By.xpath(
            "//input[contains(@placeholder,'Search') or @data-dc-search='true']");

    // Header action buttons
    private final By addRowBtn = By.xpath("//button[normalize-space()='Add Row' or contains(.,'Add Row')]");
    private final By uploadFileBtn = By.xpath("//button[normalize-space()='Upload File' or contains(.,'Upload')]");
    private final By downloadBtn = By.xpath("//button[normalize-space()='Download' or contains(.,'Download')]");
    private final By editBtn = By.xpath("//button[normalize-space()='Edit' or contains(.,'Edit') and not(contains(.,'Editor'))]");
    private final By deleteBtn = By.xpath("//button[normalize-space()='Delete' or contains(.,'Delete')]");
    private final By confirmBtn = By.xpath("//div[contains(@class,'modal-content')]//button[contains(@class,'btn-primary') or contains(.,'Yes') or contains(.,'Confirm')]");

    // Tab buttons for rate tables
    private final By rateTableTabs = By.xpath("//div[contains(@class,'nav-stretch')]//a | //ul[contains(@class,'nav-line-tabs')]//a | //div[contains(@style,'overflowY')]//button");

    public RateTablePage(WebDriver driver) {
        super(driver);
    }

    /** Clicks the button to open the Create Rate Table wizard (/rate-table/add). */
    public void clickCreateRateTable() {
        click(createRateTableBtn);
    }

    /** Enters a search query in the rate table search box. */
    public void searchRateTable(String query) {
        type(searchInput, query);
    }

    /** Clears the rate table search box. */
    public void clearSearch() {
        clearField(searchInput);
    }

    /** Clicks the "Add Row" button to insert a new row in the active rate table. */
    public void clickAddRow() {
        click(addRowBtn);
    }

    /** Clicks the "Upload File" button to open the rate table data upload modal. */
    public void clickUploadFile() {
        click(uploadFileBtn);
    }

    /** Clicks the "Download" button to export the rate table records. */
    public void clickDownload() {
        click(downloadBtn);
    }

    /** Clicks the "Edit" button to open the Rate Table edit wizard (/rate-table/edit). */
    public void clickEditRateTable() {
        click(editBtn);
    }

    /** Clicks the "Delete" button and confirms deletion. */
    public void clickDeleteRateTable() {
        click(deleteBtn);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(confirmBtn)).click();
        } catch (Exception ignored) {
        }
    }

    /** Switches to the tab of the given rate table name. */
    public void switchRateTableTab(String tableName) {
        By tabLocator = By.xpath("//a[normalize-space()=" + xpathLiteral(tableName) + "]"
                + " | //button[normalize-space()=" + xpathLiteral(tableName) + "]");
        click(tabLocator);
    }

    /** Returns all available rate table tab names. */
    public List<String> getAvailableTabs() {
        return driver.findElements(rateTableTabs).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .toList();
    }

    /** Checks whether the Rate Table page/grid is loaded. */
    @Override
    public boolean isGridLoaded() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".ag-root-wrapper, .ag-theme-alpine, [role='grid'], .flex-table-page"))).isDisplayed();
        } catch (Exception e) {
            return !driver.findElements(By.cssSelector(".ag-root-wrapper, .ag-theme-alpine, [role='grid'], .flex-table-page")).isEmpty()
                    || driver.getCurrentUrl().contains("/plans/rate-table");
        }
    }
}
