package Base;

import Pages.DashboardPage;
import Pages.LoginPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utilities.ConfigReader;

/**
 * Base class for Department Module tests. Mirrors {@link TeamsBaseTest}'s login flow but navigates
 * to Users &gt; Departments instead of Teams.
 */
public class DepartmentsBaseTest {

    @BeforeMethod
    public void setup() {
        DriverFactory.getDriver();
        DriverFactory.getDriver().get(ConfigReader.get("url"));

        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.login(ConfigReader.get("username"), ConfigReader.get("password"));

        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        dashboard.navigateToDepartments();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }
}
