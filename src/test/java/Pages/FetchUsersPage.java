package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Fetch Users page object (from fetch-users/FetchUsersList.tsx).
 *
 * <p>Lists users fetched from external streams/integrations. Allows selecting rows,
 * assigning roles via cell editors, and saving/storing fetched users.
 */
public class FetchUsersPage extends BasePage {

    private final By fetchUsersHeader = By.xpath("//*[contains(text(),'Fetch Users') or contains(text(),'User Stream')]");
    private final By selectAllCheckbox = By.xpath("(//div[contains(@class,'ag-header-select-all') or contains(@class,'ag-checkbox')])[1]");
    private final By addSelectedUsersBtn = By.xpath("//button[contains(normalize-space(),'Add User') or contains(normalize-space(),'Store Users')]");
    private final By searchInput = By.cssSelector("input[placeholder='Search'], input[placeholder='Search User']");

    public FetchUsersPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(fetchUsersHeader)).isDisplayed();
    }

    public void search(String keyword) {
        type(searchInput, keyword);
    }

    public void selectAllUsers() {
        click(selectAllCheckbox);
    }

    public void submitSelectedUsers() {
        click(addSelectedUsersBtn);
    }

    // ---- helpers for FU-001..FU-010 (read-only: Add Users is never clicked) ----

    /** True once the Fetch Users page is shown (URL + grid). */
    public boolean isFetchUsersPage() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(d -> d.getCurrentUrl().contains("fetch-users"));
            return anyVisible(By.cssSelector(".fetch-users-grid, .ag-root-wrapper"));
        } catch (Exception e) {
            return false;
        }
    }

    public java.util.List<String> columnHeaders() {
        java.util.List<String> out = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement h : driver.findElements(By.cssSelector(".ag-header-cell-text"))) {
            if (h.isDisplayed() && !h.getText().isBlank()) {
                out.add(h.getText().trim());
            }
        }
        return out;
    }

    public boolean isLegendVisible(String label, String bulletClass) {
        return anyVisible(By.xpath("//span[contains(@class,'bullet-dot') and contains(@class," + xpathLiteral(bulletClass) + ")]"
                + "/following-sibling::*[1][normalize-space()=" + xpathLiteral(label) + "] | //*[normalize-space()=" + xpathLiteral(label) + "]"
                + "[preceding-sibling::span[contains(@class,'bullet-dot') and contains(@class," + xpathLiteral(bulletClass) + ")]]"));
    }

    public int rowsWithIndicator(String bulletClass) {
        return driver.findElements(By.cssSelector(".ag-row span.bullet-dot." + bulletClass)).size();
    }

    public int rowCount() {
        return (int) driver.findElements(By.cssSelector(".ag-center-cols-container .ag-row")).stream().filter(e -> e.isDisplayed()).count();
    }

    public int selectedRowCount() {
        return (int) driver.findElements(By.cssSelector(".ag-center-cols-container .ag-row.ag-row-selected")).stream().filter(e -> e.isDisplayed()).count();
    }

    public void tickFirstRow() {
        firstVisible(By.cssSelector(".ag-pinned-left-cols-container .ag-row[row-index='0'] .ag-checkbox-input, .ag-row[row-index='0'] .ag-checkbox-input")).click();
        recordStep("Ticked first row");
    }

    public void tickHeaderCheckbox() {
        firstVisible(By.cssSelector(".ag-header .ag-header-select-all .ag-checkbox-input, .ag-header-select-all input")).click();
        recordStep("Ticked header checkbox");
    }

    public boolean isAddUsersEnabled() {
        return firstVisible(By.xpath("//button[contains(@class,'btn-primary')][.//span[normalize-space()='Add Users'] or normalize-space()='Add Users']")).isEnabled();
    }

    public String totalRecordsText() {
        return firstVisible(By.cssSelector(".vis-total-records-panel")).getText().replaceAll("\s+", " ").trim();
    }

    public void clickBack() {
        firstVisible(By.cssSelector("img[alt='backMe']")).click();
        recordStep("Clicked Back");
    }

}
