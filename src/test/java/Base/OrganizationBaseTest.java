package Base;

import Pages.LoginPage;
import Pages.OrganizationPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utilities.AutomationCleanupRegistry;
import utilities.ConfigReader;

/**
 * Base test fixture for Organization Management & Creation Module.
 * Authenticates dynamically using credentials configured in config.properties.
 */
public class OrganizationBaseTest {

    @BeforeMethod
    public void setup() {
        DriverFactory.getDriver();
        DriverFactory.getDriver().get(ConfigReader.get("url"));

        // Read login credentials directly from config.properties (prefers super admin for org management)
        String username = ConfigReader.get("super_admin.username", ConfigReader.get("username"));
        String password = ConfigReader.get("super_admin.password", ConfigReader.get("password"));

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException("Login credentials (username/password) missing in config.properties!");
        }

        // Dynamically shield active session user from any database cleanup
        AutomationCleanupRegistry.registerProtectedSessionAccount(username);
        AutomationCleanupRegistry.preFlightResetOrgs("AutoOrg Alpha", "TechCorp");

        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.login(username, password);

        OrganizationPage orgPage = new OrganizationPage(DriverFactory.getDriver());
        String currentUrl = DriverFactory.getDriver().getCurrentUrl();
        if (!currentUrl.contains("organization")) {
            orgPage.navigateToOrganizations();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }
}
