package Pages;
 
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
 
/**
 * Shared base for the three AG-Grid list pages (Users, Teams, Departments).
 *
 * <p>All three grids are structurally identical: a row is located by its visible text, its AG-Grid
 * {@code row-index} is read, row actions are reached through the shared {@code userAction{index}}
 * ellipsis button + {@code globalDropMenu} dropdown, and sort/filter live in the column header (see
 * {@link BasePage#scrollColumnIntoView} for the virtualized-column note). Everything that is common
 * lives here so the per-module pages only keep their own search/create/status/validation logic.
 */
public abstract class AgGridListPage extends BasePage {
 
    protected AgGridListPage(WebDriver driver) {
        super(driver);
    }
 
    private By rowLocator(String identifier) {
        return By.xpath("//div[@role='row' and @row-index]"
                + "[.//*[contains(normalize-space(.)," + xpathLiteral(identifier) + ")]]");
    }
 
    /** Waits for the row containing {@code identifier} and returns its AG-Grid {@code row-index}. */
    protected String rowIndexFor(String identifier) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowLocator(identifier)));
        return element.getAttribute("row-index");
    }
 
    /** Clicks the row's ellipsis button, then the named action in the shared dropdown menu. */
    protected void openAction(String identifier, String action) {
        String index = rowIndexFor(identifier);
        click(By.id("userAction" + index));
        click(By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and normalize-space()="
                + xpathLiteral(action) + "]"));
    }
 
    /** Clicks the inline "View" button in the row's action cell to open the read-only drawer. */
    public void openView(String identifier) {
        String index = rowIndexFor(identifier);
        click(By.xpath("//div[@id='userAction" + index + "']/ancestor::*[@role='gridcell'][1]"
                + "//div[normalize-space()='View']"));
    }
 
    /** Returns the capitalized status badge text (e.g. "Active" / "Inactive") for the row. */
    public String statusOf(String identifier) {
        String index = rowIndexFor(identifier);
        return text(By.xpath("//div[@row-index='" + index + "']//div[contains(@class,'badge-pill')]"));
    }
 
    /** True once a row containing {@code identifier} is rendered in the grid. Waits for it so a
     *  search/create that triggers an async grid refresh doesn't flake. */
    public boolean isRowListed(String identifier) {
        try {
            rowIndexFor(identifier);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
 
    /** Clicks an AG-Grid column header to cycle its sort state (none -> ascending -> descending). */
    public void sortByColumn(String colId) {
        scrollColumnIntoView(colId);
        click(By.xpath("//div[@col-id='" + colId + "']//span[contains(@class,'ag-header-cell-text')]"));
    }
 
    /** Reads AG-Grid's {@code aria-sort} attribute ("ascending" / "descending" / "none"). */
    public String sortDirectionOf(String colId) {
        scrollColumnIntoView(colId);
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@col-id='" + colId + "']")));
        return header.getAttribute("aria-sort");
    }
 
    /**
     * Opens the AG-Grid column menu (the hover-revealed "&#8801;" icon) for a filterable column,
     * which defaults to the Set Filter tab. This is AG-Grid's built-in per-column filter, distinct
     * from the custom "Filter" drawer opened by the funnel icon (see {@link UserFilterPage}).
     */
    public void openColumnMenu(String colId) {
        scrollColumnIntoView(colId);
        By header = By.xpath("//div[@col-id='" + colId + "']");
        WebElement headerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(header));
        new org.openqa.selenium.interactions.Actions(driver).moveToElement(headerElement).perform();
        click(By.xpath("//div[@col-id='" + colId + "']//span[contains(@class,'ag-header-cell-menu-button')]"));
    }
 
    /**
     * Toggles the first available value's checkbox in the open Set Filter list. The grids do not
     * configure {@code filterParams.buttons}, so the filter applies live (no Apply/Reset button).
     */
    public void toggleFirstColumnFilterValue() {
        click(By.xpath("(//div[contains(@class,'ag-set-filter-item')]//div[contains(@class,'ag-checkbox')])[1]"));
    }
 
    public boolean isColumnMenuOpen() {
        return !driver.findElements(By.cssSelector(".ag-menu, .ag-popup-child")).isEmpty();
    }
 
    public void closeColumnMenu() {
        driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
    }

    public boolean isGridLoaded() {
        return !driver.findElements(By.cssSelector(".ag-root-wrapper, .ag-theme-alpine")).isEmpty();
    }
}