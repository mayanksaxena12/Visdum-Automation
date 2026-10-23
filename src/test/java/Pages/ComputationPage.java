package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the Computation module (/computation/compute).
 * Handles computation runs, browsing draft/completed computation jobs in AG-Grid,
 * and statement-level approvals/exports.
 */
public class ComputationPage extends AgGridListPage {

    // Tab Locators
    private final By recentsTab = By.xpath("//a[contains(@class,'nav-link') and (normalize-space()='Recents' or contains(.,'Recents'))]");
    private final By savedDraftsTab = By.xpath("//a[contains(@class,'nav-link') and (normalize-space()='Saved Drafts' or contains(.,'Draft'))]");
    private final By scheduledTab = By.xpath("//a[contains(@class,'nav-link') and (normalize-space()='Scheduled' or contains(.,'Schedule'))]");

    // Header Action Buttons
    private final By runComputationBtn = By.xpath("//button[contains(@class,'btn-primary') and (contains(.,'Run Computation') or contains(.,'New Computation') or contains(.,'Compute'))]");
    private final By recomputeBtn = By.xpath("//button[contains(@class,'btn') and contains(.,'Recompute')]");

    // Search bar
    private final By searchInput = By.xpath("//input[contains(@placeholder,'Search') or @data-dc-search='true']");

    // Action Confirmation Modals
    private final By confirmBtn = By.xpath("//div[contains(@class,'modal-content')]//button[contains(@class,'btn-primary')]");
    private final By cancelModalBtn = By.xpath("//div[contains(@class,'modal-content')]//button[contains(@class,'btn-light') or contains(@class,'btn-secondary')]");

    public ComputationPage(WebDriver driver) {
        super(driver);
    }

    public void clickRunComputation() {
        click(runComputationBtn);
    }

    public void clickRecompute() {
        click(recomputeBtn);
    }

    public void switchToRecentsTab() {
        click(recentsTab);
    }

    public void switchToSavedDraftsTab() {
        click(savedDraftsTab);
    }

    public void switchToScheduledTab() {
        click(scheduledTab);
    }

    public void searchComputation(String query) {
        type(searchInput, query);
    }

    public void openComputation(String computationName) {
        By cellLocator = By.xpath("//div[@role='gridcell' and (contains(.,'" + computationName + "') or normalize-space()='" + computationName + "')]");
        click(cellLocator);
    }

    public void confirmAction() {
        click(confirmBtn);
    }

    public void cancelAction() {
        click(cancelModalBtn);
    }
}
