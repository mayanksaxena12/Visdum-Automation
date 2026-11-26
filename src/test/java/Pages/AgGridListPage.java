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
        String clean = identifier == null ? "" : identifier.trim().replaceAll("\\s+", " ");
        return By.xpath("//div[@role='row' and @row-index]"
                + "[.//*[contains(normalize-space(.)," + xpathLiteral(clean) + ")]]");
    }

    /**
     * An item in the shared row action menu ({@code #globalDropMenu}). The app renders it in two
     * styles: the plain Metronic one ({@code span.menu-link}) and the newer one
     * ({@code button.vis-action-menu__item} with a {@code vis-action-menu__label} span) used by the
     * Users grid. The old locator only knew the first, so Edit User / Change Password / Deactivate
     * could never be clicked on Users.
     */
    protected By actionMenuItem(String label) {
        String l = xpathLiteral(label);
        return By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and normalize-space()=" + l + "]"
                + " | //div[@id='globalDropMenu']//*[contains(@class,'vis-action-menu__item')]"
                + "[.//span[contains(@class,'vis-action-menu__label') and normalize-space()=" + l + "]]");
    }
 
    /** Waits for the row containing {@code identifier} and returns its AG-Grid {@code row-index}. */
    protected String rowIndexFor(String identifier) {
        for (int attempt = 1; ; attempt++) {
            try {
                WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        rowLocator(identifier)));
                return element.getAttribute("row-index");
            } catch (org.openqa.selenium.StaleElementReferenceException stale) {
                if (attempt >= 3) {
                    throw stale;
                }
            }
        }
    }

    /**
     * Clicks the row's ellipsis button, then the named action in the shared dropdown menu. The menu
     * closes itself if the grid re-renders or scrolls while it is open, so if the item does not show
     * up the kebab is clicked again (once) before giving up.
     */
    protected void openAction(String identifier, String action) {
        waitForGridToSettle();
        for (int attempt = 1; ; attempt++) {
            String index = rowIndexFor(identifier);
            click(By.id("userAction" + index));
            try {
                new WebDriverWait(driver, Duration.ofSeconds(6))
                        .until(ExpectedConditions.elementToBeClickable(actionMenuItem(action)));
                click(actionMenuItem(action));
                return;
            } catch (TimeoutException closed) {
                if (attempt >= 2) {
                    throw closed;
                }
                System.out.println("  [STEP] row menu closed before '" + action + "' could be clicked, reopening");
                waitForGridToSettle();
            }
        }
    }

    /**
     * Waits until the grid has stopped redrawing: no loading overlay, and the rendered rows are the
     * same on two polls in a row. Bounded (max ~10s); never fails the test by itself.
     */
    public void waitForGridToSettle() {
        final String[] last = {null};
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .pollingEvery(Duration.ofMillis(400))
                    .ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                    .until(d -> {
                        if (!d.findElements(By.cssSelector(".ag-overlay-loading-wrapper, .ag-overlay-loading-center")).isEmpty()) {
                            last[0] = null;
                            return false;
                        }
                        java.util.List<WebElement> rows = d.findElements(By.cssSelector(".ag-row"));
                        boolean noRows = !d.findElements(By.cssSelector(".ag-overlay-no-rows-wrapper, .ag-overlay-no-rows-center")).isEmpty();
                        if (rows.isEmpty() && !noRows) {
                            last[0] = null;
                            return false;
                        }
                        String signature = rows.size() + "|" + (rows.isEmpty() ? "" : rows.get(0).getText());
                        boolean stable = signature.equals(last[0]);
                        last[0] = signature;
                        return stable;
                    });
        } catch (TimeoutException ignored) {
            System.out.println("  [STEP] grid still changing after 10s, continuing");
        }
    }

    /**
     * After typing a search term: waits for the debounced server search to come back, i.e. every
     * rendered row matches the term or the grid shows "no rows". Bounded (max 15s).
     */
    public void waitForSearchResults(String term) {
        if (term == null || term.isBlank()) {
            waitForGridToSettle();
            return;
        }
        String t = term.trim().toLowerCase().replaceAll("\\s+", " ");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .pollingEvery(Duration.ofMillis(300))
                    .ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                    .until(d -> {
                        if (!d.findElements(By.cssSelector(".ag-overlay-no-rows-wrapper, .ag-overlay-no-rows-center")).isEmpty()) {
                            return true;
                        }
                        // Rows are split across pinned/center containers: join text per row-index.
                        java.util.Map<String, StringBuilder> byRow = new java.util.HashMap<>();
                        for (WebElement r : d.findElements(By.cssSelector(".ag-row[row-index]"))) {
                            byRow.computeIfAbsent(r.getAttribute("row-index"), k -> new StringBuilder())
                                    .append(' ').append(r.getText());
                        }
                        if (byRow.isEmpty()) {
                            return false;
                        }
                        for (StringBuilder text : byRow.values()) {
                            if (!text.toString().toLowerCase().replaceAll("\\s+", " ").contains(t)) {
                                return false;
                            }
                        }
                        return true;
                    });
        } catch (TimeoutException ignored) {
            System.out.println("  [STEP] search results for '" + term + "' did not settle in 15s, continuing");
        }
        waitForGridToSettle();
    }
 
    /** Clicks the inline "View" button in the row's action cell to open the read-only drawer. */
    public void openView(String identifier) {
        waitForGridToSettle();
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
        String value = text(locator).trim();
        if ("name".equalsIgnoreCase(colId)) {
            // The cell renders the avatar initials above/before the name; the name is the last line.
            String[] lines = value.split("\\R");
            for (int i = lines.length - 1; i >= 0; i--) {
                if (!lines[i].isBlank()) {
                    value = lines[i].trim();
                    break;
                }
            }
        }
        return value.replaceAll("\\s+", " ");
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
            boolean shown = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".ag-root-wrapper, .ag-theme-alpine, [role='grid']"))).isDisplayed();
            if (shown) {
                waitForGridToSettle(); // rows load (and default filters re-fetch) after the grid shell appears
            }
            return shown;
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
     * @return true only if a new, non-empty spreadsheet file appeared in the download folder
     *         ({@code test-output/downloads}) within {@code export.timeout.seconds} (default 30)
     */
    public boolean rightClickAndExport() {
        // 1. Ensure grid is loaded and has rendered either rows or its empty-state overlay
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".ag-root-wrapper, .ag-theme-alpine, [role='grid']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".ag-row, .ag-overlay-no-rows-wrapper, .ag-overlay-no-rows-center")));

        java.io.File downloadDir = new java.io.File("test-output/downloads");
        java.util.Map<String, Long> before = snapshotDownloads(downloadDir);

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

        // 9. Verify the export really produced a file (previously this always returned true)
        int timeout = Integer.parseInt(System.getProperty("export.timeout.seconds", "30"));
        java.io.File exported = waitForNewDownload(downloadDir, before, Duration.ofSeconds(timeout));
        if (exported == null) {
            System.err.println("[EXPORT] No new spreadsheet appeared in " + downloadDir.getAbsolutePath()
                    + " within " + timeout + "s.");
            return false;
        }
        boolean readable = isReadableSpreadsheet(exported);
        System.out.println("[EXPORT] Downloaded " + exported.getName() + " (" + exported.length()
                + " bytes, readable=" + readable + ")");
        return readable;
    }

    private static java.util.Map<String, Long> snapshotDownloads(java.io.File dir) {
        java.util.Map<String, Long> snap = new java.util.HashMap<>();
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File f : files) {
                snap.put(f.getName(), f.lastModified());
            }
        }
        return snap;
    }

    /** Polls for a finished (not .crdownload/.tmp), non-empty .xlsx/.xls/.csv that is new or changed. */
    private java.io.File waitForNewDownload(java.io.File dir, java.util.Map<String, Long> before, Duration timeout) {
        try {
            return new WebDriverWait(driver, timeout).pollingEvery(Duration.ofMillis(500)).until(d -> {
                java.io.File[] files = dir.listFiles();
                if (files == null) {
                    return null;
                }
                for (java.io.File f : files) {
                    String n = f.getName().toLowerCase();
                    boolean spreadsheet = n.endsWith(".xlsx") || n.endsWith(".xls") || n.endsWith(".csv");
                    Long old = before.get(f.getName());
                    boolean isNew = old == null || f.lastModified() > old;
                    if (spreadsheet && isNew && f.length() > 0) {
                        return f;
                    }
                }
                return null;
            });
        } catch (TimeoutException e) {
            return null;
        }
    }

    /** True if the file opens as a spreadsheet and has at least a header row. */
    private static boolean isReadableSpreadsheet(java.io.File f) {
        if (f.getName().toLowerCase().endsWith(".csv")) {
            return f.length() > 0;
        }
        try (java.io.InputStream in = new java.io.FileInputStream(f);
             org.apache.poi.ss.usermodel.Workbook wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(in)) {
            return wb.getNumberOfSheets() > 0 && wb.getSheetAt(0).getPhysicalNumberOfRows() > 0;
        } catch (Exception e) {
            System.err.println("[EXPORT] " + f.getName() + " is not a readable spreadsheet: " + e.getMessage());
            return false;
        }
    }
}