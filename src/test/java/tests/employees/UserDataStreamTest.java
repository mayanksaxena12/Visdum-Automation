package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.UserDataStreamPage;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the "User Data Streams" header button on the Users list page.
 * Validates either:
 * 1. The read-only User Stream view drawer if a user stream is configured.
 * 2. The Create User Stream wizard if no user stream exists yet.
 */
public class UserDataStreamTest extends BaseTest {

    @Test
    public void openUserDataStreamsFromUsersList() {
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        UserDataStreamPage userStream = new UserDataStreamPage(DriverFactory.getDriver());

        if (users.isUserDataStreamsButtonVisible()) {
            users.openUserDataStreams();

            if (userStream.isDrawerOpen()) {
                String source = userStream.getUserSource();
                Assert.assertFalse(source.isBlank(), "Expected user stream source to be displayed in drawer.");
                userStream.closeDrawer();
                Assert.assertFalse(userStream.isDrawerOpen(), "Expected user stream drawer to close.");
            } else if (userStream.isWizardOpen()) {
                userStream.cancelWizard();
                Assert.assertTrue(users.isGridLoaded() || DriverFactory.getDriver().getCurrentUrl().contains("users"),
                        "Expected to return to Users page after cancelling wizard.");
            }
        }
    }
}
