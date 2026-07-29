package Base;

import Pages.DashboardPage;
import Pages.LoginPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utilities.ConfigReader;

/**
 * Base class for Team Module tests. Mirrors {@link BaseTest}'s login flow but navigates to
 * Users &gt; Teams instead of Employees, since a single {@code @BeforeMethod} can only land on one
 * screen and the two modules are tested independently.
 */
public class TeamsBaseTest {

    @BeforeMethod
    public void setup() {

        DriverFactory.getDriver();

        DriverFactory.getDriver().get(ConfigReader.get("url"));

        LoginPage login = new LoginPage(DriverFactory.getDriver());

        login.login(ConfigReader.get("username"), ConfigReader.get("password"));

        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());

        dashboard.navigateToTeams();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }
}
