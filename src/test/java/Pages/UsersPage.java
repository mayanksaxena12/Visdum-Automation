package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * User-list actions from src/app/pages/users.
 *
 * <p>Grid row/action/sort/filter helpers (including the pinned-column row-index trick and the
 * shared {@code userAction{index}} dropdown) are inherited from {@link AgGridListPage}.
 */
public class UsersPage extends AgGridListPage {

    private final By searchBox = By.cssSelector("input[placeholder='Search'], input[placeholder='Search User']");
    private final By addNewUser = By.xpath("//*[normalize-space()='Add New User']");
    private final By fetchUsersBtn = By.xpath("//button[normalize-space()='Fetch Users']");
    private final By userDataStreamsBtn = By.xpath("//button[normalize-space()='User Data Streams' or contains(.,'User Data Streams')]");

    public UsersPage(WebDriver driver) {
        super(driver);
    }

    public void search(String value) {
        type(searchBox, value);
    }

    public void openCreateUser() {
        click(addNewUser);
    }

    public void openFetchUsers() {
        click(fetchUsersBtn);
    }

    public void openUserDataStreams() {
        click(userDataStreamsBtn);
    }

    public boolean isUserDataStreamsButtonVisible() {
        return !driver.findElements(userDataStreamsBtn).isEmpty();
    }

    public void openEditUser(String userIdentifier) {
        openAction(userIdentifier, "Edit User");
    }

    public void openChangePassword(String userIdentifier) {
        openAction(userIdentifier, "Change Password");
    }

    public void openDeactivateUser(String userIdentifier) {
        openAction(userIdentifier, "Deactivate");
    }
}
