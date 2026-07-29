package tests.employees;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DeactivateUserModal;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/** Covers the Deactivate confirmation modal (inline in UsersList.tsx), which had a Page Object
 * (DeactivateUserModal) and a UsersPage.openDeactivateUser() hook but no test exercising either. */
public class DeactivateUserTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireDeactivatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void deactivateExistingUser() {
        String user = System.getProperty("test.user.existing", "");
        if (user.isBlank()) {
            throw new IllegalArgumentException(
                    "Set -Dtest.user.existing=<email or name> for the deactivate test.");
        }
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        DeactivateUserModal modal = new DeactivateUserModal(DriverFactory.getDriver());

        users.search(user);
        Assert.assertEquals(users.statusOf(user), "Active",
                "Deactivate is only offered on the row's dropdown while the user is Active.");

        users.openDeactivateUser(user);
        // Flatpickr accepts a typed ISO date and confirms with Enter; the Submit button stays
        // disabled until a last working day is chosen (see UsersList.tsx handleDeactive()).
        String lastWorkingDay = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);
        modal.setLastWorkingDay(lastWorkingDay);
        modal.submit();

        users.search(user);
        Assert.assertEquals(users.statusOf(user), "Inactive");
    }
}
