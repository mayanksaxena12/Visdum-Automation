package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the Reports module (/reports).
 * Handles report listing in AG-Grid, searching, creating new reports,
 * opening report views, and triggering report downloads or grid exports.
 */
public class ReportsPage extends AgGridListPage {

    // Tab Locators
    private final By allReportsTab = By.xpath("//a[contains(@class,'nav-link') and (normalize-space()='All Reports' or contains(.,'All'))]");
    private final By customReportsTab = By.xpath("//a[contains(@class,'nav-link') and (normalize-space()='Custom' or contains(.,'Custom'))]");
    private final By standardReportsTab = By.xpath("//a[contains(@class,'nav-link') and (normalize-space()='Standard' or contains(.,'Standard'))]");
    private final By draftsTab = By.xpath("//a[contains(@class,'nav-link') and (normalize-space()='Saved Drafts' or contains(.,'Draft'))]");

    // Header Create Report button
    private final By createReportBtn = By.xpath("//button[contains(@class,'btn-primary') and (contains(.,'Create Report') or contains(.,'Add Report') or contains(.,'New Report'))]");

    // Search bar
    private final By searchInput = By.xpath("//input[contains(@placeholder,'Search') or @data-dc-search='true']");

    // Action Confirmation Modals
    private final By confirmBtn = By.xpath("//div[contains(@class,'modal-content')]//button[contains(@class,'btn-primary')]");
    private final By cancelModalBtn = By.xpath("//div[contains(@class,'modal-content')]//button[contains(@class,'btn-light') or contains(@class,'btn-secondary')]");

    public ReportsPage(WebDriver driver) {
        super(driver);
    }

    public void clickCreateReport() {
        click(createReportBtn);
    }

    public void switchToAllReportsTab() {
        click(allReportsTab);
    }

    public void switchToCustomReportsTab() {
        click(customReportsTab);
    }

    public void switchToStandardReportsTab() {
        click(standardReportsTab);
    }

    public void switchToDraftsTab() {
        click(draftsTab);
    }

    public void searchReport(String query) {
        type(searchInput, query);
    }

    public void openReport(String reportName) {
        By cellLocator = By.xpath("//div[@role='gridcell' and (contains(.,'" + reportName + "') or normalize-space()='" + reportName + "')]");
        click(cellLocator);
    }

    public void confirmAction() {
        click(confirmBtn);
    }

    public void cancelAction() {
        click(cancelModalBtn);
    }
}
