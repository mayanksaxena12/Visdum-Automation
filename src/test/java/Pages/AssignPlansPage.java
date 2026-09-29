package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the Plan Assignment list view (/plans/assign-plan).
 * Manages AG-Grid assignment rows, search, action menus, and opening
 * the single or bulk plan assignment wizards.
 */
public class AssignPlansPage extends AgGridListPage {

    // Action button to start Plan Assignment
    private final By assignPlanBtn = By.xpath("//button[contains(@class,'btn-primary') and (contains(.,'Assign Plan') or contains(.,'Assign') or contains(.,'Add'))]");
    private final By bulkAssignLink = By.xpath("//a[contains(@href,'bulk-add') or contains(.,'Bulk')]");

    // Search bar
    private final By searchInput = By.xpath("//input[contains(@placeholder,'Search') or @data-dc-search='true']");

    // Action Confirmation Modals
    private final By confirmBtn = By.xpath("//div[contains(@class,'modal-content')]//button[contains(@class,'btn-primary')]");
    private final By cancelModalBtn = By.xpath("//div[contains(@class,'modal-content')]//button[contains(@class,'btn-light') or contains(@class,'btn-secondary')]");

    public AssignPlansPage(WebDriver driver) {
        super(driver);
    }

    public void clickAssignPlan() {
        click(assignPlanBtn);
    }

    public void clickBulkAssign() {
        if (!driver.findElements(bulkAssignLink).isEmpty()) {
            click(bulkAssignLink);
        } else {
            driver.get(utilities.ConfigReader.get("url") + "/plans/assign-plan/bulk-add");
        }
    }

    public void searchAssignedPlan(String query) {
        type(searchInput, query);
    }

    public void clearSearch() {
        clearField(searchInput);
    }

    public void editAssignedPlan(String participantName) {
        openAction(participantName, "Edit");
    }

    public void changeStatus(String participantName) {
        openAction(participantName, "Change Status");
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmBtn)).click();
    }

    public void expirePlan(String participantName) {
        openAction(participantName, "Expire Plan");
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmBtn)).click();
    }
}
