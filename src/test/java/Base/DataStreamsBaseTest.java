package Base;

import Pages.DashboardPage;
import Pages.LoginPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utilities.ConfigReader;

/**
 * Base class for Data Stream Module tests. Mirrors {@link DepartmentsBaseTest}'s login flow but
 * navigates directly to Data Streams instead of Employees or Departments.
 */
public class DataStreamsBaseTest {

    @BeforeMethod
    public void setup() {
        DriverFactory.getDriver();
        DriverFactory.getDriver().get(ConfigReader.get("url"));

        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.login(ConfigReader.get("username"), ConfigReader.get("password"));

        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        dashboard.navigateToDataStreams();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }
}