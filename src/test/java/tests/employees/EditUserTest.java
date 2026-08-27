package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.CreateUserPage;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

public class EditUserTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireEditPermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void editUserName() {
        String user = System.getProperty("test.user.existing", "");
        if (user.isBlank()) {
            throw new IllegalArgumentException("Set -Dtest.user.existing=<email or name> for the edit test.");
        }
        String updatedName = System.getProperty("test.user.updated.name", "Updated Automation User");
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        CreateUserPage form = new CreateUserPage(DriverFactory.getDriver());
        users.search(user);
        users.openEditUser(user);
        form.updateName(updatedName);
        form.clickNextStep();
        form.saveEditedUser();

        users.search(updatedName);
        Assert.assertTrue(users.isRowListed(updatedName),
                "Expected the edited user '" + updatedName + "' to be listed.");
    }
}
