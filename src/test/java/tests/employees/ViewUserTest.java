package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.UserViewPage;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Covers the read-only "View" drawer (UserActionModals/UserView.tsx), which had a Page Object
 * (UserViewPage) but no test exercising it. */
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
}
