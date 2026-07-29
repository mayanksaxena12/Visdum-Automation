package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Team-list actions from src/app/pages/teams (TeamsList.tsx / TeamWrapper.tsx).
 *
 * <p>Same AG-Grid + shared {@code globalDropMenu} dropdown pattern as {@link UsersPage}: a row is
 * located by its visible text, its {@code row-index} is read, and actions are targeted through the
 * {@code userAction{index}} ellipsis button rendered by {@code TeamActionsCell}.
 */
public class TeamsPage extends BasePage {

    private final By searchBox = By.cssSelector("input[placeholder='Search']");
    private final By createTeam = By.xpath("//*[normalize-space()='Create New Team']");

    public TeamsPage(WebDriver driver) {
        super(driver);
    }

    public void search(String value) {
        type(searchBox, value);
    }

    public void openCreateTeam() {
        click(createTeam);
    }

    /** Waits for the row containing {@code identifier} and returns its AG-Grid {@code row-index}. */
    private String rowIndexFor(String identifier) {
        By row = By.xpath("//div[@role='row' and @row-index]"
                + "[.//*[contains(normalize-space(.)," + xpathLiteral(identifier) + ")]]");
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(row));
        return element.getAttribute("row-index");
    }

    private void openAction(String teamIdentifier, String action) {
        String index = rowIndexFor(teamIdentifier);
        click(By.id("userAction" + index));
        click(By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and normalize-space()="
                + xpathLiteral(action) + "]"));
    }

    public void openEditTeam(String teamIdentifier) {
        openAction(teamIdentifier, "Edit");
    }

    public void openAddMembers(String teamIdentifier) {
        openAction(teamIdentifier, "Add Members");
    }

    /** Opens the Deactivate/Activate confirmation modal (label flips with the team's current status). */
    public void openToggleStatus(String teamIdentifier) {
        String index = rowIndexFor(teamIdentifier);
        click(By.id("userAction" + index));
        click(By.xpath("//div[@id='globalDropMenu']//span[contains(@class,'menu-link') and "
                + "(normalize-space()='Deactivate' or normalize-space()='Activate')]"));
    }

    /** Clicks the inline "View" button in the team's action cell to open the read-only drawer. */
    public void openView(String teamIdentifier) {
        String index = rowIndexFor(teamIdentifier);
        click(By.xpath("//div[@id='userAction" + index + "']/ancestor::*[@role='gridcell'][1]"
                + "//div[normalize-space()='View']"));
    }

    /** Returns the capitalized status badge text (e.g. "Active" / "Inactive") for the team's row. */
    public String statusOf(String teamIdentifier) {
        String index = rowIndexFor(teamIdentifier);
        return text(By.xpath("//div[@row-index='" + index + "']//div[contains(@class,'badge-pill')]"));
    }

    public boolean isTeamListed(String teamIdentifier) {
        return !driver.findElements(By.xpath("//div[@role='row' and @row-index]"
                + "[.//*[contains(normalize-space(.)," + xpathLiteral(teamIdentifier) + ")]]")).isEmpty();
    }

    /**
     * Clicks an AG-Grid column header to cycle its sort state (none -> ascending -> descending).
     * {@code colId} is the underlying field from table/_columns.tsx (e.g. "id", "name", "status").
     */
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
     * Opens the AG-Grid column menu (the hover-revealed menu icon) for "Team Name"/"Status" -- the
     * only two columns in table/_columns.tsx with {@code filter: 'agSetColumnFilter'}.
     */
    public void openColumnMenu(String colId) {
        scrollColumnIntoView(colId);
        By header = By.xpath("//div[@col-id='" + colId + "']");
        WebElement headerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(header));
        new org.openqa.selenium.interactions.Actions(driver).moveToElement(headerElement).perform();
        click(By.xpath("//div[@col-id='" + colId + "']//span[contains(@class,'ag-header-cell-menu-button')]"));
    }

    /**
     * Toggles the first available value's checkbox in the open Set Filter list. Like the Users
     * grid, table/_columns.tsx does not configure {@code filterParams.buttons}, so this filters the
     * grid live with no separate Apply/Reset button. Toggling the same checkbox again restores the
     * original (unfiltered) state.
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
}
