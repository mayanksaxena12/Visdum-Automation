package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
// import org.openqa.selenium.WebElement;
// import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Department-list actions from src/app/pages/department (DepartmentsList.tsx / DepartmentWrapper.tsx).
 *
 * <p>Structurally identical to {@link TeamsPage}: same AG-Grid + shared {@code globalDropMenu}
 * dropdown pattern, with the row-action ellipsis rendered by {@code DepartmentActionsCell} as
 * {@code userAction{index}}. Common grid helpers are inherited from {@link AgGridListPage}.
 */
public class DepartmentsPage extends AgGridListPage {

    private final By searchBox = By.cssSelector("input[placeholder='Search']");
    private final By createDepartment = By.xpath("//*[normalize-space()='Create New Department']");

    public DepartmentsPage(WebDriver driver) {
        super(driver);
    }

    public void search(String value) {
        type(searchBox, value);
    }

    public void openCreateDepartment() {
        click(createDepartment);
    }

    // private String rowIndexFor(String identifier) {
    //     By row = By.xpath("//div[@role='row' and @row-index]"
    //             + "[.//*[contains(normalize-space(.)," + xpathLiteral(identifier) + ")]]");
    //     WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(row));
    //     return element.getAttribute("row-index");
    // }

    // private void openAction(String departmentIdentifier, String action) {
    //     String index = rowIndexFor(departmentIdentifier);
    //     click(By.id("userAction" + index));
    //     click(By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and normalize-space()="
    //             + xpathLiteral(action) + "]"));
    // }

    public void openEditDepartment(String departmentIdentifier) {
        openAction(departmentIdentifier, "Edit");
    }

    public void openAddMembers(String departmentIdentifier) {
        openAction(departmentIdentifier, "Add Members");
    }

    /** Opens the Deactivate/Activate confirmation modal (label flips with the department's status). */
    public void openToggleStatus(String departmentIdentifier) {
        String index = rowIndexFor(departmentIdentifier);
        click(By.id("userAction" + index));
        click(By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and "
                + "(normalize-space()='Deactivate' or normalize-space()='Activate')]"));
    }

    // public void openView(String departmentIdentifier) {
    //     String index = rowIndexFor(departmentIdentifier);
    //     click(By.xpath("//div[@id='userAction" + index + "']/ancestor::*[@role='gridcell'][1]"
    //             + "//div[normalize-space()='View']"));
    // }

    // public String statusOf(String departmentIdentifier) {
    //     String index = rowIndexFor(departmentIdentifier);
    //     return text(By.xpath("//div[@row-index='" + index + "']//div[contains(@class,'badge-pill')]"));
    // }

    // public boolean isDepartmentListed(String departmentIdentifier) {
    //     return !driver.findElements(By.xpath("//div[@role='row' and @row-index]"
    //             + "[.//*[contains(normalize-space(.)," + xpathLiteral(departmentIdentifier) + ")]]")).isEmpty();
    // }

    // /** Clicks an AG-Grid column header to cycle its sort state (none -> ascending -> descending). */
    // public void sortByColumn(String colId) {
    //     scrollColumnIntoView(colId);
    //     click(By.xpath("//div[@col-id='" + colId + "']//span[contains(@class,'ag-header-cell-text')]"));
    // }

    // public String sortDirectionOf(String colId) {
    //     scrollColumnIntoView(colId);
    //     WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
    //             By.xpath("//div[@col-id='" + colId + "']")));
    //     return header.getAttribute("aria-sort");
    // }

    // /** Opens the AG-Grid column menu (Set Filter) for the "Department Name"/"Status" columns. */
    // public void openColumnMenu(String colId) {
    //     scrollColumnIntoView(colId);
    //     By header = By.xpath("//div[@col-id='" + colId + "']");
    //     WebElement headerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(header));
    //     new org.openqa.selenium.interactions.Actions(driver).moveToElement(headerElement).perform();
    //     click(By.xpath("//div[@col-id='" + colId + "']//span[contains(@class,'ag-header-cell-menu-button')]"));
    // }

    // /** Toggles the first Set Filter value; like Teams/Users, the filter applies live (no Apply button). */
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
