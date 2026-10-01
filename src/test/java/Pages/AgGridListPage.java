package Pages;
 
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.ScreenRecorderUtil;
 
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

    /** Returns the text of the first rendered row for a given column id (e.g. "name"). */
    public String getFirstRowValue(String colId) {
        scrollColumnIntoView(colId);
        By locator;
        if ("name".equalsIgnoreCase(colId)) {
            locator = By.xpath("//div[@role='row' and @row-index='0']//div[@col-id='name']//div[contains(@class,'text-truncate')]//span");
            if (driver.findElements(locator).isEmpty()) {
                locator = By.xpath("//div[@role='row' and @row-index='0']//div[@col-id='name']");
            }
        } else {
            locator = By.xpath("//div[@role='row' and @row-index='0']//div[@col-id='" + colId + "']");
        }
        return text(locator).trim();
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
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".ag-root-wrapper, .ag-theme-alpine, [role='grid']"))).isDisplayed();
        } catch (Exception e) {
            return !driver.findElements(By.cssSelector(".ag-root-wrapper, .ag-theme-alpine, [role='grid']")).isEmpty();
        }
    }

    /**
     * Checks if the AG-Grid right-click context menu is currently open.
     */
    public boolean isContextMenuOpen() {
        return !driver.findElements(By.cssSelector(".ag-menu, .ag-popup-child, .ag-menu-list")).isEmpty();
    }

    /**
     * Performs a right-click (context-click) on the AG-Grid data table,
     * hovers/clicks the "Export" menu option, clicks "Excel Export", and triggers the export download.
     *
     * @return boolean true if Export context menu and Excel Export action was successfully triggered
     */
    public boolean rightClickAndExport() {
        // 1. Ensure grid is loaded
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".ag-root-wrapper, .ag-theme-alpine, [role='grid']")));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        // 2. Locate an element inside the grid to right-click
        WebElement targetElement = null;
        By[] candidates = new By[] {
            By.cssSelector(".ag-row[row-index='0'] .ag-cell"),
            By.cssSelector(".ag-row .ag-cell"),
            By.cssSelector(".ag-cell[role='gridcell']"),
            By.cssSelector(".ag-center-cols-container .ag-row"),
            By.cssSelector(".ag-center-cols-viewport"),
            By.cssSelector(".ag-body-viewport"),
            By.cssSelector(".ag-root-wrapper")
        };

        for (By candidate : candidates) {
            java.util.List<WebElement> found = driver.findElements(candidate);
            if (!found.isEmpty()) {
                for (WebElement el : found) {
                    if (el.isDisplayed()) {
                        targetElement = el;
                        break;
                    }
                }
                if (targetElement != null) break;
            }
        }

        if (targetElement == null) {
            targetElement = driver.findElement(By.cssSelector(".ag-root-wrapper"));
        }

        // 3. Right-click on targetElement
        Actions actions = new Actions(driver);
        actions.contextClick(targetElement).perform();

        // 4. Wait for AG-Grid context menu popup
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement menu = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".ag-menu, .ag-popup-child, .ag-menu-list")));

        // 5. Locate the "Export" menu option
        By exportOptionLocator = By.xpath("//div[contains(@class,'ag-menu-option') and (.//*[normalize-space()='Export'] or @data-action='export')]");
        WebElement exportOption = shortWait.until(ExpectedConditions.visibilityOfElementLocated(exportOptionLocator));

        // 6. Hover or click over "Export" option to trigger AG-Grid sub-menu
        actions.moveToElement(exportOption).perform();

        By excelExportLocator = By.xpath("//div[contains(@class,'ag-menu-option') and (.//*[normalize-space()='Excel Export'] or contains(.,'Excel Export'))]");
        WebElement excelExportOption = null;
        try {
            excelExportOption = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(excelExportLocator));
        } catch (Exception e) {
            // If hovering didn't trigger popup in headless mode, click the export option
            exportOption.click();
            excelExportOption = shortWait.until(ExpectedConditions.visibilityOfElementLocated(excelExportLocator));
        }

        // 7. Click "Excel Export"
        excelExportOption.click();

        // 8. Capture frame in video recording
        ScreenRecorderUtil.captureFrame(driver, "AG-Grid Context Menu: Export -> Excel Export Clicked");

        // 9. Allow export request and download trigger to process
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        return true;
    }
}