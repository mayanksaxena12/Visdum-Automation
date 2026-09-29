package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the User Data Streams flow (from users/user-stream/ and UsersWrapper.tsx).
 * Handles both:
 * 1. The read-only drawer (_UserStreamDetailView.tsx) displayed when a user stream already exists.
 * 2. The multi-step wizard (_CreateUserStream.tsx) displayed when creating a new user stream.
 */
public class UserDataStreamPage extends BasePage {

    // Drawer locators
    private final By userSourceHeader = By.xpath("//*[contains(@class,'card')]//span[normalize-space()='User Source']");
    private final By drawerCloseBtn = By.xpath("//button[contains(@id,'_close')] | //div[contains(@class,'card-footer') or contains(@id,'_footer')]//div[normalize-space()='Back'] | //button[normalize-space()='Back'] | //button[normalize-space()='Cancel']");

    // Wizard locators
    private final By wizardTitle = By.xpath("//*[contains(@class,'page-title') or contains(@class,'step-title') or self::h1 or self::h2 or self::h3][contains(normalize-space(),'User Data Stream') or contains(normalize-space(),'Stream Setup')]");
    private final By wizardCancelBtn = By.xpath("//div[contains(@class,'step-footer')]//button[normalize-space()='Cancel'] | //button[normalize-space()='Cancel']");

    public UserDataStreamPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDrawerOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(userSourceHeader)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserSource() {
        By sourceText = By.xpath("//div[./span[normalize-space()='User Source']]//*[contains(@class,'text-dark') or contains(@class,'fw-bold')]");
        return text(sourceText).trim();
    }

    public void closeDrawer() {
        click(drawerCloseBtn);
    }

    public boolean isWizardOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(wizardTitle)).isDisplayed()
                    || driver.getCurrentUrl().contains("user-streams");
        } catch (Exception e) {
            return driver.getCurrentUrl().contains("user-streams");
        }
    }

    public void cancelWizard() {
        click(wizardCancelBtn);
    }
}
