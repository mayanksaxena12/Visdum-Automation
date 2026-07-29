package tests.login;

import Base.DriverFactory;
import Base.LoginBaseTest;
import Pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ConfigReader;

/**
 * Valid credentials should redirect to the 2-step verification screen. The OTP entry itself is
 * manual (needs a real emailed code), so this only asserts the redirect.
 */
public class ValidLoginTest extends LoginBaseTest {

    @Test
    public void validCredentialsRedirectTo2FA() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.login(ConfigReader.get("username"), ConfigReader.get("password"));
        Assert.assertTrue(login.is2FADisplayed(),
                "Valid login should land on the 2-step verification screen.");
    }
}
