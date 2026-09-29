package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the Plans list view (/plans or /plans/create-plan).
 * Manages AG-Grid operations, search, tabs (Recents, Templates, Saved Drafts),
 * and opening the Advance or Simple Plan wizards.
 */
public class PlansPage extends AgGridListPage {

    // Tab Locators
    private final By recentsTab = By.xpath("//a[contains(@class,'nav-link') and normalize-space()='Recents']");
    private final By templatesTab = By.xpath("//a[contains(@class,'nav-link') and normalize-space()='Templates']");
    private final By savedDraftsTab = By.xpath("//a[contains(@class,'nav-link') and normalize-space()='Saved Drafts']");

    // Header Create Plan button & Dropdown Options
    private final By createPlanBtn = By.xpath("//button[contains(@class,'btn-primary') and (contains(.,'Create Plan') or contains(.,'Add Plan'))]");
    private final By advancePlanOption = By.xpath("//*[contains(@class,'menu-link') or contains(@class,'menu-item')][contains(.,'Advance')]");
    private final By simplePlanOption = By.xpath("//*[contains(@class,'menu-link') or contains(@class,'menu-item')][contains(.,'Simple')]");

    // Search bar
    private final By searchInput = By.xpath("//input[contains(@placeholder,'Search') or @data-dc-search='true']");

    // Action Confirmation Modals
    private final By confirmBtn = By.xpath("//div[contains(@class,'modal-content')]//button[contains(@class,'btn-primary')]");
    private final By cancelModalBtn = By.xpath("//div[contains(@class,'modal-content')]//button[contains(@class,'btn-light') or contains(@class,'btn-secondary')]");

    public PlansPage(WebDriver driver) {
        super(driver);
    }

    public void clickCreatePlanAdvance() {
        click(createPlanBtn);
        click(advancePlanOption);
    }

    public void clickCreatePlanSimple() {
        click(createPlanBtn);
        click(simplePlanOption);
    }

    public void switchToRecentsTab() {
        click(recentsTab);
    }

    public void switchToSavedDraftsTab() {
        click(savedDraftsTab);
    }

    public void switchToTemplatesTab() {
        click(templatesTab);
    }

    public void searchPlan(String query) {
        type(searchInput, query);
    }

    public void clearSearch() {
        clearField(searchInput);
    }

    public void clonePlan(String planName) {
        openAction(planName, "Clone");
    }

    public void editPlan(String planName) {
        openAction(planName, "Edit");
    }

    public void deletePlan(String planName) {
        openAction(planName, "Delete");
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmBtn)).click();
    }

    public void changePlanStatus(String planName) {
        openAction(planName, "Change Status");
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmBtn)).click();
    }
}
