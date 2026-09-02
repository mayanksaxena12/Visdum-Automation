package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Read-only user drawer from UserActionModals/UserView.tsx.
 *
 * <p>Each field is an {@code <li>} holding two spans: a label span ({@code fs-6 text-gray-500}) and
 * a value span ({@code fs-6} without the muted class). We locate the value by finding the list item
 * whose label matches, then selecting the sibling {@code fs-6} span that is not the muted label.
 */
public class UserViewPage extends BasePage {

    private final By backButton = By.xpath("//button[normalize-space()='Back']");
    private final By viewManagerHistoryLink = By.xpath("//a[normalize-space()='View Manager History']");
    private final By managerHistoryHeader = By.xpath("//h4[normalize-space()='Manager History']");
    private final By closeManagerHistoryBtn = By.xpath("//div[./h4[normalize-space()='Manager History']]//button");

    public UserViewPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(backButton)).isDisplayed();
    }

    private String value(String label) {
        return text(By.xpath("//li[.//span[normalize-space()=" + xpathLiteral(label) + "]]"
                + "//span[contains(@class,'fs-6') and not(contains(@class,'text-gray-500'))]"));
    }

    public String getName() {
        return value("Name");
    }

    public String getEmail() {
        return value("E-mail");
    }

    public String getReferenceId() {
        return value("User Reference ID");
    }

    public String getRole() {
        return value("Role");
    }

    public String getCurrency() {
        return value("Currency");
    }

    public String getEmployeeId() {
        return value("Employee Id");
    }

    public String getJoiningDate() {
        return value("Joining Date");
    }

    public String getDesignation() {
        return value("Designation");
    }

    public String getBankDetails() {
        return value("Bank Details");
    }

    public String getTaxDetails() {
        return value("Tax Details");
    }

    public String getManager() {
        return value("Manager");
    }

    public String getManagerEffectiveDate() {
        return value("Manager Effective Date");
    }

    public String getTeam() {
        return value("Team");
    }

    public String getDepartment() {
        return value("Department");
    }

    public void openManagerHistory() {
        click(viewManagerHistoryLink);
    }

    public boolean isManagerHistoryOpen() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(managerHistoryHeader)).isDisplayed();
    }

    public void closeManagerHistory() {
        click(closeManagerHistoryBtn);
    }

    public void close() {
        click(backButton);
    }
}

