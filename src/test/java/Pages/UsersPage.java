package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * User-list actions from src/app/pages/users.
 *
 * <p>Grid row/action/sort/filter helpers (including the pinned-column row-index trick and the
 * shared {@code userAction{index}} dropdown) are inherited from {@link AgGridListPage}.
 */
public class UsersPage extends AgGridListPage {

    private final By searchBox = By.cssSelector("input[placeholder='Search'], input[placeholder='Search User']");
    private final By addNewUser = By.xpath("//button[normalize-space()='Add New User' or normalize-space()='Add User' or normalize-space()='Create new user'] | //*[normalize-space()='Add New User']");
    private final By fetchUsersBtn = By.xpath("//button[normalize-space()='Fetch Users']");
    private final By userDataStreamsBtn = By.xpath("//button[normalize-space()='User Data Streams' or contains(.,'User Data Streams')]");

    public UsersPage(WebDriver driver) {
        super(driver);
    }

    public void search(String value) {
        type(searchBox, value);
        waitForSearchResults(value); // the search is debounced and filtered on the server
    }

    public void openCreateUser() {
        // Clicking before the grid has rendered is sometimes ignored, leaving the drawer closed.
        By nameInput = By.name("name");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".ag-row, .ag-overlay-no-rows-wrapper, .ag-overlay-no-rows-center")));
        for (int attempt = 1; attempt <= 2; attempt++) {
            click(addNewUser);
            try {
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.visibilityOfElementLocated(nameInput));
                return;
            } catch (org.openqa.selenium.TimeoutException notOpenYet) {
                System.out.println("  [STEP] Add New User drawer did not open (attempt " + attempt + ")");
            }
        }
        throw new IllegalStateException("Add New User drawer did not open after 2 clicks.");
    }

    // ---- read-only checks used by the Users-sheet ID mappings (UL-*, KM-*) ----

    private boolean visible(By locator) {
        return anyVisible(locator);
    }

    /** True if a grid column header with this exact text is shown (e.g. "Name", "E-mail", "Action"). */
    public boolean isColumnHeaderVisible(String headerText) {
        return visible(By.xpath("//div[@role='columnheader']//*[normalize-space()=" + xpathLiteral(headerText) + "]"));
    }

    /** The grid footer text, e.g. "Total Records: 94"; empty if not shown. */
    public String totalRecordsText() {
        By footer = By.xpath("//span[starts-with(normalize-space(),'Total Records')]/..");
        return visible(footer) ? driver.findElement(footer).getText().replaceAll("\\s+", " ").trim() : "";
    }

    /** "Add New User" etc. are not always <button> elements, so match any element with that text. */
    public boolean isPageButtonVisible(String label) {
        String l = xpathLiteral(label);
        return visible(By.xpath("//button[normalize-space()=" + l + "] | //a[normalize-space()=" + l + "]"
                + " | //*[contains(@class,'btn') and normalize-space()=" + l + "]"));
    }

    public boolean isFilterIconVisible() {
        return visible(By.cssSelector("i.bi-funnel, i.bi-funnel-fill"));
    }

    public boolean hasActiveStatusBadge() {
        return visible(By.xpath("//div[@col-id='status']//*[contains(@class,'badge-pill')][contains(normalize-space(),'Active')]"));
    }

    /** Clicks the row's three-dot (kebab) button. {@code rowIndex} is AG-Grid's row-index (0 = first row). */
    public void openRowMenu(int rowIndex) {
        waitForGridToSettle();
        click(By.id("userAction" + rowIndex));
    }

    /** Number of rows currently rendered in the grid. */
    public int renderedRowCount() {
        return driver.findElements(By.cssSelector(".ag-center-cols-container .ag-row")).size();
    }

    public boolean isRowMenuItemVisible(String label) {
        return visible(actionMenuItem(label));
    }

    /** Number of row action menus currently shown (0 when closed). */
    public int openRowMenuCount() {
        return (int) driver.findElements(By.xpath("//div[@id='globalDropMenu']")).stream()
                .filter(e -> {
                    try {
                        String cls = e.getAttribute("class");
                        return e.isDisplayed() && cls != null && cls.contains("show");
                    } catch (Exception ex) {
                        return false;
                    }
                }).count();
    }

    /** Clicks on the page heading, i.e. outside any open menu. */
    public void clickOutside() {
        click(By.xpath("//*[normalize-space()='Create and Manage Users']"));
    }

    public void openFetchUsers() {
        click(fetchUsersBtn);
    }

    public void openUserDataStreams() {
        click(userDataStreamsBtn);
    }

    public boolean isUserDataStreamsButtonVisible() {
        return !driver.findElements(userDataStreamsBtn).isEmpty();
    }

    public void openEditUser(String userIdentifier) {
        openAction(userIdentifier, "Edit User");
    }

    public void openChangePassword(String userIdentifier) {
        openAction(userIdentifier, "Change Password");
    }

    public void openDeactivateUser(String userIdentifier) {
        openAction(userIdentifier, "Deactivate");
    }

    // ---- helpers for UL-009 / UL-011 ----
    /** Number of rendered rows that have their three-dot (kebab) action button. */
    public int rowsWithKebab() {
        int count = 0;
        for (org.openqa.selenium.WebElement row : driver.findElements(By.cssSelector(".ag-row[row-index]"))) {
            String idx = row.getAttribute("row-index");
            if (!driver.findElements(By.id("userAction" + idx)).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /** Scrolls the grid horizontally to an off-screen column and reports whether its header shows. */
    public boolean isColumnReachable(String colId) {
        scrollColumnIntoView(colId);
        boolean shown = anyVisible(By.xpath("//div[@role='columnheader' and @col-id=" + xpathLiteral(colId) + "]"));
        recordStep("Scrolled to column '" + colId + "': " + shown);
        return shown;
    }

}
