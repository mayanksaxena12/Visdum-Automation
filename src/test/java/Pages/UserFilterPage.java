package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Advanced/custom filter drawer shared by list pages (opened via the funnel icon in
 * UsersList.tsx and rendered by components/CustomeFilter/CustomFilter.tsx).
 *
 * <p>Only rendered when the logged-in user has the {@code USER_STREAM} permission -- the funnel
 * icon lives inside that permission check in UsersList.tsx, so {@link #openFilterPanel()} will
 * time out for a UAT user that lacks it.
 *
 * <p>The Operator and Value choices are fetched from the backend at runtime, so this page object
 * only picks the column deterministically and accepts whichever operator/value option the control
 * highlights by default (see {@link BasePage#selectFirstReactOption}), rather than hard-coding
 * labels that could drift.
 */
public class UserFilterPage extends BasePage {

    private final By funnelIcon = By.cssSelector("i.bi-funnel, i.bi-funnel-fill");
    private final By enterValueInput = By.cssSelector("input[placeholder='Enter value *']");
    private final By applyFilters = By.xpath("//button[normalize-space()='Apply Filters']");
    private final By clearAll = By.xpath("//button[normalize-space()='Clear All']");

    public UserFilterPage(WebDriver driver) {
        super(driver);
    }

    public void openFilterPanel() {
        click(funnelIcon);
    }

    public void selectColumn(String columnHeaderName) {
        selectReactOption("Select Column *", columnHeaderName);
    }

    public void selectFirstOperator() {
        selectFirstReactOption("Select Operator *");
    }

    /** Handles either RHS shape: a react-select ("Select Value *") or a free-text input ("Enter value *"). */
    public void setValueBestEffort(String textValue) {
        if (!driver.findElements(By.xpath("//*[normalize-space()='Select Value *']")).isEmpty()) {
            selectFirstReactOption("Select Value *");
            return;
        }
        type(enterValueInput, textValue);
    }

    public void applyFilters() {
        click(applyFilters);
    }

    public void clearAllFilters() {
        click(clearAll);
    }

    public boolean isPanelOpen() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(applyFilters)).isDisplayed();
    }
}
