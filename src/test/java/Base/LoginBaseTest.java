package Base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utilities.ConfigReader;

/**
 * Base class for Login-page tests. Unlike the other module base classes it does NOT log in --
 * it simply lands on the login page so the validation/error/redirect flows can be exercised.
 */
public class LoginBaseTest {

    @BeforeMethod
    public void openLoginPage() {
        WebDriver driver = DriverFactory.getDriver();
        driver.get(ConfigReader.get("url"));
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }
}
