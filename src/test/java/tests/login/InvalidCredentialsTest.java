package tests.login;

import java.time.Instant;

import Base.DriverFactory;
import Base.LoginBaseTest;
import Pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ConfigReader;

/** Invalid-credential and unregistered-email error flows (read-only; no OTP required). */
public class InvalidCredentialsTest extends LoginBaseTest {

    @Test
    public void wrongPasswordShowsInvalidCredentials() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.enterEmail(ConfigReader.get("username"));
        login.enterPassword("WrongPass_" + Instant.now().toEpochMilli());
        login.clickLogin();
        Assert.assertTrue(login.isMessageVisible("Invalid Credentials"),
                "Expected the invalid-credentials error message.");
    }

    @Test
    public void unregisteredEmailShowsError() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.enterEmail("noone_" + Instant.now().toEpochMilli() + "@yopmail.com");
        login.enterPassword("SomePass@123");
        login.clickLogin();
        Assert.assertTrue(login.isMessageVisible("email is invalid"),
                "Expected the 'selected email is invalid' error message.");
    }
}
