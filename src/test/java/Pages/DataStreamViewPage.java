package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Read-only Data Stream view drawer (from data-streams/view-stream/DataStreamView.tsx
 * and _StreamDetailView.tsx).
 *
 * <p>Rendered inside an ActionDrawer with fields: Stream Name, Data Source, Status,
 * Stream Type, Department, etc., and a "Back" button in the footer.
 */
public class DataStreamViewPage extends BasePage {

    private final By streamNameHeader = By.xpath("//*[contains(@class,'card')]//span[normalize-space()='Stream Name']");
    private final By backBtn = By.xpath("//div[contains(@class,'card-footer') or contains(@id,'_footer')]//div[normalize-space()='Back'] | //button[contains(@id,'_close')] | //button[normalize-space()='Back']");

    public DataStreamViewPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(streamNameHeader)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private String fieldText(String label) {
        By locator = By.xpath("//div[./span[normalize-space()=" + xpathLiteral(label) + "]]"
                + "//*[contains(@class,'text-dark') or contains(@class,'badge-pill') or contains(@class,'fw-bold')][not(normalize-space()=" + xpathLiteral(label) + ")]");
        return text(locator).trim();
    }

    public String getStreamName() {
        return fieldText("Stream Name");
    }

    public String getStreamType() {
        return fieldText("Stream Type");
    }

    public String getStatus() {
        return fieldText("Status");
    }

    public String getDepartment() {
        return fieldText("Department");
    }

    public void close() {
        click(backBtn);
    }
}
