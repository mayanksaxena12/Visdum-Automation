package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.FetchUsersPage;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Covers the "Fetch Users" button and User Stream page flow.
 */
public class FetchUsersTest extends BaseTest {

    @Test
    public void openFetchUsersPageFromUserList() {
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        FetchUsersPage fetchPage = new FetchUsersPage(DriverFactory.getDriver());

        users.openFetchUsers();
        Assert.assertTrue(fetchPage.isLoaded(), "Expected Fetch Users / Stream page to load after clicking 'Fetch Users'.");
    }
}
