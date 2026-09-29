package tests.employees;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import Base.DriverFactory;
import Base.UserModuleTest;
import Pages.DeactivateUserModal;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;
import utilities.TestUser;

/**
 * Covers the Deactivate confirmation modal (inline in UsersList.tsx).
 * Includes both non-destructive cancellation tests (safe to run read-only)
 * and destructive deactivation flow gated by ExecutionGuard.
 */
public class DeactivateUserTest extends UserModuleTest {

    private String resolveUserName(UsersPage users) {
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
    public void verifyDeactivateModalCanBeCancelled() {
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        DeactivateUserModal modal = new DeactivateUserModal(DriverFactory.getDriver());

        String user = resolveUserName(users);
        users.search(user);

        if (users.isRowListed(user)) {
            String initialStatus = users.statusOf(user);
            if ("Active".equalsIgnoreCase(initialStatus)) {
                users.openDeactivateUser(user);
                Assert.assertTrue(modal.isOpen(), "Expected Deactivate modal to open.");

                modal.cancel();
                Assert.assertFalse(modal.isOpen(), "Expected Deactivate modal to close on cancel.");
                Assert.assertEquals(users.statusOf(user), initialStatus,
                        "User status must remain unchanged after cancelling modal.");
            }
        }
    }

    @Test
    public void deactivateExistingUser() {
        ExecutionGuard.requireDestructiveTestsEnabled();

        String user = System.getProperty("test.user.existing", "");
        if (user.isBlank()) {
            TestUser created = createActiveUser();
            user = created.name;
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
        modal.selectProcessPayout(true);
        modal.submit();

        users.search(user);
        Assert.assertEquals(users.statusOf(user), "Inactive");
    }
}
