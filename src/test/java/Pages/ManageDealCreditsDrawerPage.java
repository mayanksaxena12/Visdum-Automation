package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Manage Deal Credits drawer page object
 * (from deal-credits/components/ManageDealCreditsDrawer.tsx).
 */
public class ManageDealCreditsDrawerPage extends BasePage {

    private final By drawerHeader = By.xpath("//*[contains(@class,'card-header') or contains(@id,'_header')][.//*[contains(.,'Manage Deal Credits')]]");
    private final By searchStreamsInput = By.xpath("//input[@placeholder='Search data streams...']");
    private final By closeDrawerBtn = By.xpath("//button[contains(@id,'_close')]");

    public ManageDealCreditsDrawerPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(searchStreamsInput)).isDisplayed()
                    || wait.until(ExpectedConditions.visibilityOfElementLocated(drawerHeader)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void searchDataStreams(String keyword) {
        type(searchStreamsInput, keyword);
    }

    public boolean isDataStreamListed(String name) {
        return !driver.findElements(By.xpath("//div[contains(@class,'rounded')][.//span[normalize-space()=" + xpathLiteral(name) + "]]")).isEmpty();
    }

    public void clickEditDataStream(String name) {
        click(By.xpath("//div[contains(@class,'rounded')][.//span[normalize-space()=" + xpathLiteral(name) + "]]//button[contains(@class,'manage-dc-edit-btn')]"));
    }

    public void close() {
        click(closeDrawerBtn);
    }
}
