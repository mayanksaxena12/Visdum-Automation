package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
// import org.openqa.selenium.WebElement;
// import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * User-list actions from src/app/pages/users.
 *
 * <p>Grid row/action/sort/filter helpers (including the pinned-column row-index trick and the
 * shared {@code userAction{index}} dropdown) are inherited from {@link AgGridListPage}.
 */
public class UsersPage extends AgGridListPage {

    private final By searchBox = By.cssSelector("input[placeholder='Search'], input[placeholder='Search User']");
    private final By addNewUser = By.xpath("//*[normalize-space()='Add New User']");
    private final By fetchUsersBtn = By.xpath("//button[normalize-space()='Fetch Users']");

    public UsersPage(WebDriver driver) {
        super(driver);
    }

    public void search(String value) {
        type(searchBox, value);
    }

    public void openCreateUser() {
        click(addNewUser);
    }

    public void openFetchUsers() {
        click(fetchUsersBtn);
    }

    // /** Waits for the row containing {@code identifier} and returns its AG-Grid {@code row-index}. */
    // private String rowIndexFor(String identifier) {
    //     By row = By.xpath("//div[@role='row' and @row-index]"
    //             + "[.//*[contains(normalize-space(.)," + xpathLiteral(identifier) + ")]]");
    //     WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(row));
    //     return element.getAttribute("row-index");
    // }

    // public void openAction(String userIdentifier, String action) {
    //     String index = rowIndexFor(userIdentifier);
    //     click(By.id("userAction" + index));
    //     click(By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and normalize-space()="
    //             + xpathLiteral(action) + "]"));
    // }

    public void openEditUser(String userIdentifier) {
        openAction(userIdentifier, "Edit User");
    }

    public void openChangePassword(String userIdentifier) {
        openAction(userIdentifier, "Change Password");
    }

    public void openDeactivateUser(String userIdentifier) {
        openAction(userIdentifier, "Deactivate");
    }

    // /** Clicks the inline "View" button in the user's action cell to open the read-only drawer. */
    // public void openView(String userIdentifier) {
    //     String index = rowIndexFor(userIdentifier);
    //     click(By.xpath("//div[@id='userAction" + index + "']/ancestor::*[@role='gridcell'][1]"
    //             + "//div[normalize-space()='View']"));
    // }

    // /** Returns the capitalized status badge text (e.g. "Active" / "Inactive") for the user's row. */
    // public String statusOf(String userIdentifier) {
    //     String index = rowIndexFor(userIdentifier);
    //     return text(By.xpath("//div[@row-index='" + index + "']//div[contains(@class,'badge-pill')]"));
    // }

    // public boolean isUserListed(String userIdentifier) {
    //     return !driver.findElements(By.xpath("//div[@role='row' and @row-index]"
    //             + "[.//*[contains(normalize-space(.)," + xpathLiteral(userIdentifier) + ")]]")).isEmpty();
    // }

    // /**
    //  * Clicks an AG-Grid column header to cycle its sort state (none -> ascending -> descending).
    //  * {@code colId} is the underlying field name from table/_columns.tsx (e.g. "name", "email").
    //  */
    // public void sortByColumn(String colId) {
    //     scrollColumnIntoView(colId);
    //     click(By.xpath("//div[@col-id='" + colId + "']//span[contains(@class,'ag-header-cell-text')]"));
    // }

    // /** Reads AG-Grid's {@code aria-sort} attribute ("ascending" / "descending" / "none"). */
    // public String sortDirectionOf(String colId) {
    //     scrollColumnIntoView(colId);
    //     WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
    //             By.xpath("//div[@col-id='" + colId + "']")));
    //     return header.getAttribute("aria-sort");
    // }

    // /**
    //  * Opens the AG-Grid column menu (the "&#8801;" icon shown on hover in each sortable/filterable
    //  * header from table/_columns.tsx) which defaults to the Filter tab since {@code menuTabs} lists
    //  * {@code filterMenuTab} first. This is AG-Grid's built-in per-column Set Filter, distinct from
    //  * the custom "Filter" drawer opened by the funnel icon (see {@link UserFilterPage}).
    //  */
    // public void openColumnMenu(String colId) {
    //     scrollColumnIntoView(colId);
    //     By header = By.xpath("//div[@col-id='" + colId + "']");
    //     WebElement headerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(header));
    //     // The menu button only renders in the DOM once the header is hovered/focused.
    //     new org.openqa.selenium.interactions.Actions(driver).moveToElement(headerElement).perform();
    //     click(By.xpath("//div[@col-id='" + colId + "']//span[contains(@class,'ag-header-cell-menu-button')]"));
    // }

    // /**
    //  * Toggles the first available value's checkbox in the open Set Filter list. table/_columns.tsx
    //  * does not configure {@code filterParams.buttons}, so AG-Grid's default Set Filter has no
    //  * separate Apply/Reset button -- unlike the custom drawer, it filters the grid live as soon as
    //  * a checkbox changes. Values start fully selected (no filter applied); one toggle excludes that
    //  * value, and toggling the same checkbox again restores the original (unfiltered) state.
    //  */
    // public void toggleFirstColumnFilterValue() {
    //     click(By.xpath("(//div[contains(@class,'ag-set-filter-item')]//div[contains(@class,'ag-checkbox')])[1]"));
    // }

    // public boolean isColumnMenuOpen() {
    //     return !driver.findElements(By.cssSelector(".ag-menu, .ag-popup-child")).isEmpty();
    // }

    // public void closeColumnMenu() {
    //     driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
    // }
}
