package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Details drawer opened from Raw Data row action "View Details"
 * (from raw-data/view-raw-data/RawDataDealCreditView.tsx).
 */
public class RawDataViewPage extends BasePage {

    private final By drawerContent = By.xpath("//div[contains(@id,'action_drawer') or contains(@class,'drawer')]//div[contains(@class,'card')]");
    private final By backBtn = By.xpath("//div[contains(@class,'card-footer') or contains(@id,'_footer')]//div[normalize-space()='Back']"
            + " | //button[contains(@id,'_close')]"
            + " | //button[normalize-space()='Back']");

    public RawDataViewPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(backBtn)).isDisplayed()
                    || wait.until(ExpectedConditions.visibilityOfElementLocated(drawerContent)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void close() {
        click(backBtn);
    }
}
