package utilities;

import java.util.HashMap;
import java.util.Map;

//import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import Pages.DashboardPage;
import Pages.DepartmentViewPage;
import Pages.DepartmentsPage;
import Pages.LoginPage;
import Pages.TeamViewPage;
import Pages.TeamsPage;
import Pages.UserViewPage;
import Pages.UsersPage;

/**
 * Central registry mapping a data-driven row's {@code Module:Scenario} to a concrete
 * {@link TestAction} built from the existing Page Objects.
 *
 * <p>Dispatch is keyed by <b>Module:Scenario</b> (not by individual Test Case ID) so that adding a
 * new test case that reuses an existing scenario -- e.g. sorting a different column -- is purely a
 * new row in the control file with a different {@code Param1}, requiring <b>zero code changes</b>.
 * A genuinely new scenario type is the only thing that needs a new entry here.
 *
 * <p>All actions are read-only/idempotent so they are safe to run in parallel against shared data.
 * {@code Param1}/{@code Param2} carry the scenario's data (e.g. a search term or an AG-Grid col id).
 */
public final class TestCaseRegistry {

    private static final Map<String, TestAction> ACTIONS = new HashMap<>();

    static {
        // ---------------- User module ----------------
        register("user:search", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            page.search(row.param1());
            // Assert.assertTrue(driver.getPageSource().contains(row.param1()),
            //         "Expected search results to contain '" + row.param1() + "'.");
            Assert.assertTrue(page.isRowListed(row.param1()),
                    "Expected a user row to match '" + row.param1() + "'.");
        });
        register("user:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "none");
            page.sortByColumn(row.param1());
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "ascending",
                    row.param1() + " should sort ascending.");
        });
        register("user:columnfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            page.openColumnMenu(row.param1());
            Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for " + row.param1());
            page.toggleFirstColumnFilterValue();
            page.closeColumnMenu();
        });
        register("user:view", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            page.search(row.param1());
            page.openView(row.param1());
            Assert.assertTrue(new UserViewPage(driver).isOpen(), "User view drawer should open.");
        });

        // ---------------- Team module ----------------
        register("team:search", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.search(row.param1());
            // Assert.assertTrue(driver.getPageSource().contains(row.param1()),
            //         "Expected team search results to contain '" + row.param1() + "'.");
            Assert.assertTrue(page.isRowListed(row.param1()),
                    "Expected a team row to match '" + row.param1() + "'.");
        });
        register("team:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "none");
            page.sortByColumn(row.param1());
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "ascending",
                    row.param1() + " should sort ascending.");
        });
        register("team:columnfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.openColumnMenu(row.param1());
            Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for " + row.param1());
            page.toggleFirstColumnFilterValue();
            page.closeColumnMenu();
        });
        register("team:view", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.search(row.param1());
            page.openView(row.param1());
            Assert.assertTrue(new TeamViewPage(driver).isOpen(), "Team view drawer should open.");
        });

        // ---------------- Department module ----------------
        register("department:search", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.search(row.param1());
            // Assert.assertTrue(driver.getPageSource().contains(row.param1()),
            //         "Expected department search results to contain '" + row.param1() + "'.");
            Assert.assertTrue(page.isRowListed(row.param1()),
                    "Expected a department row to match '" + row.param1() + "'.");
        });
        register("department:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "none");
            page.sortByColumn(row.param1());
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "ascending",
                    row.param1() + " should sort ascending.");
        });
        register("department:columnfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.openColumnMenu(row.param1());
            Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for " + row.param1());
            page.toggleFirstColumnFilterValue();
            page.closeColumnMenu();
        });
        register("department:view", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.search(row.param1());
            page.openView(row.param1());
            Assert.assertTrue(new DepartmentViewPage(driver).isOpen(), "Department view drawer should open.");
        });

        // ---------------- Login module (validation / error / redirect; no OTP needed) ----------------
        // These actions assume the driver is already on the login page (the Excel runner does NOT
        // pre-login for the Login module).
        register("login:validlogin", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "Valid login should reach the 2-step verification screen.");
        });
        register("login:invalidpassword", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail(ConfigReader.get("username"));
            lp.enterPassword("WrongPass_" + java.time.Instant.now().toEpochMilli());
            lp.clickLogin();
            Assert.assertTrue(lp.isMessageVisible("Invalid Credentials"),
                    "Expected the invalid-credentials error.");
        });
        register("login:invalidemail", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail("noone_" + java.time.Instant.now().toEpochMilli() + "@yopmail.com");
            lp.enterPassword("SomePass@123");
            lp.clickLogin();
            Assert.assertTrue(lp.isMessageVisible("email is invalid"),
                    "Expected the 'selected email is invalid' error.");
        });
        register("login:emptyfields", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Email is required"));
            Assert.assertTrue(lp.isFieldErrorVisible("Password is required"));
        });
        register("login:emailonly", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail(ConfigReader.get("username"));
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Password is required"));
        });
        register("login:passwordonly", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterPassword("SomePass@123");
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Email is required"));
        });
        register("login:emailformat", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail("email@");
            lp.enterPassword("SomePass@123");
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Wrong email format"));
        });
        register("login:emaillength", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail("a".repeat(45) + "@example.com");
            lp.enterPassword("SomePass@123");
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Maximum 50 symbols"));
        });
        register("login:passwordlength", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail(ConfigReader.get("username"));
            lp.enterPassword("Ab@1");
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Minimum 8 symbols"));
        });

        // ---------------- 2FA / OTP flow (OTP read live from the UAT database) ----------------
        // The OTP actions below all start from the login page: they submit valid credentials,
        // wait for the 2-step verification screen, fetch the latest OTP from two_factor_authentications
        // (ORDER BY id DESC) and then assert the requested behavior.
 
        register("login:otpsent", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(),
                    "Valid login should reach the 2-step verification screen.");
        });
 
        register("login:otp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            String otp = utilities.OtpDbReader.latestOtp();
            lp.enterOtp(otp);
            lp.clickVerify();
            Assert.assertTrue(new DashboardPage(driver).isLoaded(),
                    "Valid OTP '" + otp + "' should log the user in and land on the dashboard.");
        });
 
        register("login:wrongotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            lp.enterOtp("000000");
            lp.clickVerify();
            Assert.assertTrue(lp.isStillOn2FA(),
                    "Wrong OTP should keep the user on the 2-step verification screen.");
        });
 
        register("login:emptyotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            lp.clickVerify();
            Assert.assertTrue(lp.isStillOn2FA(),
                    "Submitting no OTP should keep the user on the 2-step verification screen.");
        });
 
        register("login:shortotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            lp.enterOtp("123");
            lp.clickVerify();
            Assert.assertTrue(lp.isStillOn2FA(),
                    "A partial OTP should keep the user on the 2-step verification screen.");
        });
 
        register("login:alphaotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            lp.enterOtp("abc123");
            lp.clickVerify();
            Assert.assertTrue(lp.isStillOn2FA(),
                    "An OTP containing alphabets should keep the user on the 2-step verification screen.");
        });
 
        register("login:pasteotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            String otp = utilities.OtpDbReader.latestOtp();
            lp.pasteOtp(otp);
            Assert.assertTrue(lp.isOtpFilled(otp),
                    "Pasting a 6-digit OTP should auto-fill all digit boxes.");
        });
 
        register("login:otpformat", (driver, row) -> {
            String otp = utilities.OtpDbReader.latestOtp();
            Assert.assertTrue(otp.matches("\\d{6}"),
                    "two_factor_code '" + otp + "' should be exactly 6 numeric digits.");
        });
    
    }

    private TestCaseRegistry() {
    }

    private static void register(String key, TestAction action) {
        ACTIONS.put(key, action);
    }

    public static TestAction get(String actionKey) {
        return ACTIONS.get(actionKey);
    }

    public static boolean isMapped(String actionKey) {
        return ACTIONS.containsKey(actionKey);
    }
}
