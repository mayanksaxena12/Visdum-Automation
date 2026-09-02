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
}
