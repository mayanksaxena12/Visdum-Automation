package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.UserViewPage;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Covers the read-only "View" drawer (UserActionModals/UserView.tsx) and Manager History drawer. */
public class ViewUserTest extends BaseTest {

    private String resolveUser(UsersPage users) {
        String user = System.getProperty("test.user.existing", "");
        if (user.isBlank()) {
            try {
                user = users.getFirstRowValue("name");
            } catch (Exception ignored) {
            }
        }
        if (user.isBlank()) {
            user = "Mayank";
        }
        return user;
    }

    @Test
    public void viewUserShowsSearchedRecord() {
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        UserViewPage view = new UserViewPage(DriverFactory.getDriver());

        String user = resolveUser(users);
        users.search(user);
        users.openView(user);

        Assert.assertTrue(view.isOpen(), "Expected the user view drawer to open.");
        Assert.assertTrue(view.getName().toLowerCase().contains(user.toLowerCase()),
                "Expected the drawer's Name field to contain the searched value.");

        view.close();
    }

    @Test
    public void viewUserManagerHistoryOpensDrawer() {
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        UserViewPage view = new UserViewPage(DriverFactory.getDriver());

        String user = resolveUser(users);
        users.search(user);
        users.openView(user);

        Assert.assertTrue(view.isOpen(), "Expected the user view drawer to open.");

        view.openManagerHistory();
        Assert.assertTrue(view.isManagerHistoryOpen(), "Expected the Manager History drawer to open.");

        view.closeManagerHistory();
        view.close();
    }
}

