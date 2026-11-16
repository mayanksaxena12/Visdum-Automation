package Base;

import Pages.CreateUserPage;
import Pages.UsersPage;
import org.testng.annotations.AfterMethod;
import utilities.ExcelTestDataReader;
import utilities.TestUser;
import utilities.UserCleanupUtil;

import java.util.Map;

/**
 * Base class for User Module tests.
 * Test data is driven directly from manual-testcases.xlsx (Sheet: Users).
 * Automatically executes post-test cleanup to keep the environment reset across runs.
 */
public class UserModuleTest extends BaseTest {

    private final ThreadLocal<String> createdUserEmail = new ThreadLocal<>();

    protected TestUser createActiveUser() {
        // Read valid test user definition directly from Excel row CS1-012
        Map<String, String> excelData = ExcelTestDataReader.getTestDataMap("Users", "CS1-012");

        String name = excelData.getOrDefault("Name", "Test User New");
        String email = excelData.getOrDefault("Email", "newuser@yopmail.com");
        String refId = excelData.getOrDefault("Ref ID", "TEST001");
        String role = excelData.getOrDefault("Role", "Individual Contributor");
        if ("IC".equalsIgnoreCase(role)) {
            role = "Individual Contributor";
        }
        String currency = excelData.getOrDefault("Currency", "USD");

        // Fetch password from Excel CS3-010 or configuration
        String password = excelData.getOrDefault("Password", "Password@123");

        // Clean up any stale user from previous runs before creation
        UserCleanupUtil.cleanupUserByEmail(email);
        UserCleanupUtil.cleanupUserByRefId(refId);

        TestUser user = new TestUser(name, email, refId, "EMP-" + refId, password);
        createdUserEmail.set(email);

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

    @AfterMethod(alwaysRun = true)
    public void cleanupCreatedUser() {
        String email = createdUserEmail.get();
        if (email != null && !email.isBlank()) {
            UserCleanupUtil.cleanupUserByEmail(email);
            createdUserEmail.remove();
        }
    }
}
