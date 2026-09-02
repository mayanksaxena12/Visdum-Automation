package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Data Streams page object (from data-streams/DataStreamsWrapper.tsx).
 *
 * <p>Lists active and draft data streams, supports search, viewing, editing,
 * and opening the 6-step Create Data Stream wizard.
 */
public class DataStreamsPage extends BasePage {

    private final By headerTitle = By.xpath("//*[normalize-space()='Data Streams']");
    private final By searchInput = By.cssSelector("input[placeholder='Search Here'], input[placeholder='Search']");
    private final By createStreamBtn = By.xpath("//button[contains(normalize-space(),'Create')]");
    private final By activeTab = By.xpath("//*[normalize-space()='Active']");
    private final By draftTab = By.xpath("//*[normalize-space()='Draft']");

    public DataStreamsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(headerTitle)).isDisplayed();
        } catch (Exception e) {
            return driver.getCurrentUrl().contains("data-streams");
        }
    }

    public void search(String keyword) {
        type(searchInput, keyword);
    }

    public void clickCreateDataStream() {
        try {
            By btn = By.xpath("//button[contains(normalize-space(),'Create')] | //a[contains(@href,'data-streams/add')] | //*[contains(text(),'Create data stream')]");
            click(btn);
        } catch (Exception e) {
            driver.get(utilities.ConfigReader.get("url") + "/data/data-streams/add");
        }
    }

    public void switchToActiveTab() {
        click(activeTab);
    }

    public void switchToDraftTab() {
        click(draftTab);
    }

    public boolean isStreamListed(String streamName) {
        return !driver.findElements(By.xpath("//*[contains(text()," + xpathLiteral(streamName) + ")]")).isEmpty();
    }

    public void openViewStream(String streamName) {
        click(By.xpath("//*[contains(text()," + xpathLiteral(streamName) + ")]/ancestor::tr//button[contains(text(),'View')]"));
    }

    public void openEditStream(String streamName) {
        click(By.xpath("//*[contains(text()," + xpathLiteral(streamName) + ")]/ancestor::tr//button[contains(text(),'Edit')]"));
    }
}
