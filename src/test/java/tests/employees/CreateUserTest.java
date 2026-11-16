package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.CreateUserPage;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExcelTestDataReader;
import utilities.ExecutionGuard;
import utilities.UserCleanupUtil;

import java.util.Map;

/**
 * Excel-Driven Automated Test Suite for User Creation.
 * Reads test data strictly from src/test/resources/manual-testcases.xlsx (Sheet: Users).
 * Automatically cleans up created test users after execution.
 */
public class CreateUserTest extends BaseTest {

    private final ThreadLocal<String> createdUserEmail = new ThreadLocal<>();
    private final ThreadLocal<String> createdUserRefId = new ThreadLocal<>();

    @BeforeMethod(alwaysRun = true)
    public void requireCreatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test(description = "Verify user creation with valid mandatory fields driven from Excel row CS1-012")
    public void createValidUser() {
        // Read test data directly from Excel workbook
        Map<String, String> data = ExcelTestDataReader.getTestDataMap("Users", "CS1-012");

        String userName = data.getOrDefault("Name", "Test User New");
        String email = data.getOrDefault("Email", "newuser@yopmail.com");
        String refId = data.getOrDefault("Ref ID", "TEST001");
        String role = data.getOrDefault("Role", "Individual Contributor");
        if ("IC".equalsIgnoreCase(role)) {
            role = "Individual Contributor";
        }
        String currency = data.getOrDefault("Currency", "USD");
        String password = data.getOrDefault("Password", "Password@123");

        // Pre-test cleanup to guarantee unique key availability
        UserCleanupUtil.cleanupUserByEmail(email);
        UserCleanupUtil.cleanupUserByRefId(refId);

        createdUserEmail.set(email);
        createdUserRefId.set(refId);

        UsersPage users = new UsersPage(DriverFactory.getDriver());
        CreateUserPage form = new CreateUserPage(DriverFactory.getDriver());

        users.openCreateUser();
        form.enterPersonalDetails(userName, email, refId, role, currency);
        form.clickNextStep();
        form.enterOfficialDetails("EMP-" + refId, null, null, null);
        form.clickNextStep();
        form.enterPasswordDetails(password);
        form.submitNewUser();
        utilities.AutomationCleanupRegistry.trackCreatedUser("CS1-012", null, email);

        users.search(userName);
        Assert.assertTrue(users.isRowListed(userName),
                "Expected created user '" + userName + "' to be listed in the grid.");
    }

    @Test(description = "Verify user creation with all fields populated driven from Excel row CS3-010")
    public void createValidUserWithAllFields() {
        // Read full profile test data from Excel row CS3-010
        Map<String, String> data = ExcelTestDataReader.getTestDataMap("Users", "CS3-010");

        String userName = data.getOrDefault("Name", "Full Auto User");
        String email = data.getOrDefault("Email", "fullauto@yopmail.com");
        String refId = data.getOrDefault("Ref ID", "FULLAUTO01");
        String role = data.getOrDefault("Role", "Individual Contributor");
        if ("IC".equalsIgnoreCase(role)) {
            role = "Individual Contributor";
        }
        String currency = data.getOrDefault("Currency", "USD");
        String password = data.getOrDefault("Password", "Password@123");

        UserCleanupUtil.cleanupUserByEmail(email);
        UserCleanupUtil.cleanupUserByRefId(refId);

        createdUserEmail.set(email);
        createdUserRefId.set(refId);

        UsersPage users = new UsersPage(DriverFactory.getDriver());
        CreateUserPage form = new CreateUserPage(DriverFactory.getDriver());

        users.openCreateUser();
        form.enterPersonalDetails(userName, email, refId, role, currency);
        form.clickNextStep();

        form.enterOfficialDetails("EMP-" + refId, null, "Senior Automation Engineer",
                null, null, null, null, "HDFC Bank - 123456789", "PAN-ABCDE1234F");
        form.clickNextStep();

        form.enterPasswordDetails(password, true);
        form.submitNewUser();
        utilities.AutomationCleanupRegistry.trackCreatedUser("CS3-010", null, email);

        users.search(userName);
        Assert.assertTrue(users.isRowListed(userName),
                "Expected full user '" + userName + "' to be listed in the grid.");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanupCreatedUser() {
        String email = createdUserEmail.get();
        if (email != null && !email.isBlank()) {
            UserCleanupUtil.cleanupUserByEmail(email);
            createdUserEmail.remove();
        }
        String refId = createdUserRefId.get();
        if (refId != null && !refId.isBlank()) {
            UserCleanupUtil.cleanupUserByRefId(refId);
            createdUserRefId.remove();
        }
    }
}
