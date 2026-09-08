package Pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for Settings > Resource module (ResourceSettings.tsx / ResourceTable.tsx).
 *
 * <p>Extends {@link AgGridListPage} for AG-Grid virtualized scroll, column sorting,
 * Set Filter column menus, and row actions.
 */
public class ResourcesPage extends AgGridListPage {

    private final By heading = By.xpath("//h3[normalize-space()='Resource']");
    private final By addResourceBtn = By.xpath("//div[contains(@class,'btn-primary') and contains(.,'Resource')] | //button[contains(.,'Resource')]");
    private final By searchBox = By.id("filter-box");
    private final By gridRows = By.xpath("//div[@role='row' and @row-index]");
    private final By noRows = By.xpath("//*[contains(@class,'ag-overlay-no-rows-center') or contains(text(),'No Rows To Show')]");

    public ResourcesPage(WebDriver driver) {
        super(driver);
    }

    /** True if the Resource page header and grid are loaded. */
    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(heading)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** True if the AG-Grid element is present on the page. */
    public boolean isGridLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".ag-theme-alpine"))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Opens the Add Resource modal by clicking "+ Resource". */
    public void openAddResource() {
        click(addResourceBtn);
    }

    /** Types into the quick search box to filter the grid. */
    public void search(String value) {
        type(searchBox, value);
        try {
            Thread.sleep(500); // Allow quick-filter debounce to take effect
        } catch (InterruptedException ignored) {}
    }

    /** Clears the quick search box to reset the grid. */
    public void clearSearch() {
        clearField(searchBox);
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox)).sendKeys(Keys.ESCAPE);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
    }

    /** Returns the current text in the search input. */
    public String getSearchValue() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox)).getAttribute("value");
    }

    /** Returns the number of currently rendered rows in the grid. */
    public int getRowCount() {
        return driver.findElements(gridRows).size();
    }

    /** True if AG-Grid's "No Rows To Show" overlay is displayed. */
    public boolean isNoRowsOverlayVisible() {
        return !driver.findElements(noRows).isEmpty();
    }

    /**
     * Clicks the inline "View" button for the resource row matching {@code identifier}
     * to open the Preview modal.
     */
    @Override
    public void openView(String identifier) {
        String index = rowIndexFor(identifier);
        By viewBtn = By.xpath("//div[contains(@class,'ag-pinned-right-cols-container')]//div[@role='row' and @row-index='" + index + "']//div[contains(@class,'btn') and normalize-space()='View']"
                + " | //div[@role='row' and @row-index='" + index + "']//div[contains(@class,'btn') and normalize-space()='View']");
        click(viewBtn);
    }

    /**
     * Clicks the row's 3-dots action menu and selects "Edit" to open the Update Resource modal.
     */
    public void openEditResource(String identifier) {
        openRowDropdownAction(identifier, "Edit");
    }

    /**
     * Clicks the row's 3-dots action menu and selects "Delete" to open the Delete confirmation modal.
     */
    public void openDeleteResource(String identifier) {
        openRowDropdownAction(identifier, "Delete");
    }

    /**
     * Helper to open the 3-dots ellipsis menu for a row and click an item in globalDropMenu.
     */
    private void openRowDropdownAction(String identifier, String actionText) {
        String index = rowIndexFor(identifier);
        By ellipsisBtn = By.xpath("//div[contains(@class,'ag-pinned-right-cols-container')]//div[@role='row' and @row-index='" + index + "']//div[contains(@class,'btn-active-secondary') or contains(@id,'userAction')]"
                + " | //div[@role='row' and @row-index='" + index + "']//div[contains(@class,'btn-active-secondary') or contains(@id,'userAction')]");
        click(ellipsisBtn);

        By menuItem = By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and normalize-space()="
                + xpathLiteral(actionText) + "] | //div[@id='globalDropMenu']//*[normalize-space()=" + xpathLiteral(actionText) + "]");
        click(menuItem);
    }

    /**
     * Returns true if a row containing {@code identifier} is currently displayed in the grid.
     */
    @Override
    public boolean isRowListed(String identifier) {
        try {
            By row = By.xpath("//div[@role='row' and @row-index][.//*[contains(normalize-space(.),"
                    + xpathLiteral(identifier) + ")]]");
            return wait.until(ExpectedConditions.visibilityOfElementLocated(row)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Returns the cell value in the first row for a given column (e.g. "name", "roles", "type").
     */
    @Override
    public String getFirstRowValue(String colId) {
        scrollColumnIntoView(colId);
        By locator = By.xpath("//div[@role='row' and @row-index='0']//div[@col-id='" + colId + "']");
        return text(locator).trim();
    }
}
