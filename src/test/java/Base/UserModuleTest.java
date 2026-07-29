package Base;

import java.time.Instant;

import Pages.CreateUserPage;
import Pages.UsersPage;
import utilities.TestUser;

/**
 * Base class for User Module tests. Extends {@link BaseTest} (login + navigate to Employees) and
 * adds a fixture that creates a fresh, active user so destructive/read tests have a known target.
 *
 * <p>Role and currency default to values every UAT org has, but can be overridden with
 * {@code -Dtest.user.role=...} and {@code -Dtest.user.currency=...} if needed.
 */
public class UserModuleTest extends BaseTest {

    protected TestUser createActiveUser() {
        String id = String.valueOf(Instant.now().toEpochMilli());
        String role = System.getProperty("test.user.role", "Individual Contributor");
        String currency = System.getProperty("test.user.currency", "INR");

        TestUser user = new TestUser(
                "Automation User " + id,
                "automation." + id + "@example.test",
                "AUTO-" + id,
                "EMP-" + id,
                "Test@1234");

        UsersPage users = new UsersPage(DriverFactory.getDriver());
        CreateUserPage form = new CreateUserPage(DriverFactory.getDriver());

        users.openCreateUser();
        form.enterPersonalDetails(user.name, user.email, user.referenceId, role, currency);
        form.clickNextStep();
        form.enterOfficialDetails(user.employeeNumber, null, null, null);
        form.clickNextStep();
        form.enterPasswordDetails(user.password);
        form.submitNewUser();

        return user;
    }
}
