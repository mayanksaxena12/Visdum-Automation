package tests.employees;

import java.time.Instant;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.CreateUserPage;
import Pages.UsersPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

public class CreateUserTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireCreatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void createValidUser() {
        String id = String.valueOf(Instant.now().toEpochMilli());
        String password = System.getProperty("test.user.password", "Test@1234");
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        CreateUserPage form = new CreateUserPage(DriverFactory.getDriver());

        users.openCreateUser();
        form.enterPersonalDetails("Automation User " + id, "automation." + id + "@example.test",
                "AUTO-" + id, System.getProperty("test.user.role", "Individual Contributor"),
                System.getProperty("test.user.currency", "INR"));
        form.clickNextStep();
        form.enterOfficialDetails("EMP-" + id, System.getProperty("test.user.manager"),
                System.getProperty("test.user.team"), System.getProperty("test.user.department"));
        form.clickNextStep();
        form.enterPasswordDetails(password);
        form.submitNewUser();
    }
}
