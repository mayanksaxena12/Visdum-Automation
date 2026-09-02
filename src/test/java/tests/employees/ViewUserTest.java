package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.UserViewPage;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Covers the read-only "View" drawer (UserActionModals/UserView.tsx) and Manager History drawer. */
public class ViewUserTest extends BaseTest {

    @Test
    public void viewUserShowsSearchedRecord() {
        String user = System.getProperty("test.user.existing", "Mayank");

        UsersPage users = new UsersPage(DriverFactory.getDriver());
        UserViewPage view = new UserViewPage(DriverFactory.getDriver());

        users.search(user);
        users.openView(user);

        Assert.assertTrue(view.isOpen(), "Expected the user view drawer to open.");
        Assert.assertTrue(view.getName().toLowerCase().contains(user.toLowerCase()),
                "Expected the drawer's Name field to contain the searched value.");

        view.close();
    }

    @Test
    public void viewUserManagerHistoryOpensDrawer() {
        String user = System.getProperty("test.user.existing", "Mayank");

        UsersPage users = new UsersPage(DriverFactory.getDriver());
        UserViewPage view = new UserViewPage(DriverFactory.getDriver());

        users.search(user);
        users.openView(user);

        Assert.assertTrue(view.isOpen(), "Expected the user view drawer to open.");

        view.openManagerHistory();
        Assert.assertTrue(view.isManagerHistoryOpen(), "Expected the Manager History drawer to open.");

        view.closeManagerHistory();
        view.close();
    }
}

