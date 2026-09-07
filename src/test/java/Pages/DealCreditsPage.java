package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Deal Credits page object (from deal-credits/DealCreditsWrapper.tsx,
 * DealCreditsListHeader.tsx, and DealCreditsTable.tsx).
 *
 * <p>Extends {@link AgGridListPage} to leverage AG-Grid row matching, sorting,
 * and column virtualization.
 */
public class DealCreditsPage extends AgGridListPage {

    private final By headerTitle = By.xpath("//*[normalize-space()='Deal Credits']");
    private final By searchInput = By.cssSelector("input[placeholder='Search Here'], input[placeholder='Search']");
    private final By manageDealCreditsBtn = By.id("manage_deal_credits");
    private final By processDealCreditsBtn = By.id("process_deal_credits");
    private final By fullscreenBtn = By.xpath("//span[contains(@class,'expand-container')]");

    // Process Deal Credits Modal
    private final By processModalTitle = By.xpath("//div[contains(@class,'modal-title') and contains(.,'Process Deal Credits')]");
    private final By processModalCancelBtn = By.xpath("//div[contains(@class,'modal')]//button[normalize-space()='Cancel']");
    private final By processModalConfirmBtn = By.xpath("//div[contains(@class,'modal')]//button[normalize-space()='Process' or .//span[normalize-space()='Process']]");

    public DealCreditsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(headerTitle)).isDisplayed()
                    || wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput)).isDisplayed();
        } catch (Exception e) {
            return driver.getCurrentUrl().contains("deal-credits");
        }
    }

    public void search(String keyword) {
        type(searchInput, keyword);
    }

    public void clearSearch() {
        clearField(searchInput);
    }

    public void clickManageDealCredits() {
        click(manageDealCreditsBtn);
    }

    public void clickProcessDealCredits() {
        click(processDealCreditsBtn);
    }

    public boolean isProcessModalOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(processModalTitle)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void cancelProcessModal() {
        click(processModalCancelBtn);
    }

    public void confirmProcessModal() {
        click(processModalConfirmBtn);
    }

    public void toggleFullScreen() {
        click(fullscreenBtn);
    }
}
