package Base;

import Pages.DashboardPage;
import Pages.LoginPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utilities.ConfigReader;

/**
 * Base class for Resources Module tests. Logs into the application and navigates
 * to Settings > Resource tab before each test.
 */
public class ResourcesBaseTest {

    @BeforeMethod
    public void setup() {
        DriverFactory.getDriver();
        DriverFactory.getDriver().get(ConfigReader.get("url"));

        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.login(ConfigReader.get("username"), ConfigReader.get("password"));

        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        dashboard.navigateToResources();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }
}
