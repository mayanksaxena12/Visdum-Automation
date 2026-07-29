package Base;

import Pages.DashboardPage;
import Pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;
import utilities.ConfigReader;

public class BaseTest {

    @BeforeMethod
    public void setup() {
        WebDriver driver = DriverFactory.getDriver();
        driver.get(ConfigReader.get("url"));

        LoginPage login = new LoginPage(driver);
        login.login(ConfigReader.get("username"), ConfigReader.get("password"));

        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.navigateToEmployees();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }
}
