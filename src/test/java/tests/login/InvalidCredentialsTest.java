package tests.login;

import java.time.Instant;
import java.util.Map;

import Base.DriverFactory;
import Base.LoginBaseTest;
import Pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ConfigReader;
import utilities.ExcelTestDataReader;

/**
 * Invalid-credential and unregistered-email error flows.
 * Uses test data patterns driven by manual-testcases.xlsx (Sheet: Login, TC_004, TC_005).
 */
public class InvalidCredentialsTest extends LoginBaseTest {

    @Test(description = "TC_004: Verify login with incorrect password shows Invalid Credentials")
    public void wrongPasswordShowsInvalidCredentials() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        Map<String, String> data = ExcelTestDataReader.getTestDataMap("Login", "TC_011");
        String basePassword = data.getOrDefault("Password", "InvalidPass@123");

        login.enterEmail(ConfigReader.get("username"));
        login.enterPassword(basePassword + "_Wrong");
        login.clickLogin();
        Assert.assertTrue(login.isMessageVisible("Invalid Credentials"),
                "Expected the invalid-credentials error message.");
    }

    @Test(description = "TC_005: Verify login with unregistered email shows error")
    public void unregisteredEmailShowsError() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        String testPassword = "InvalidPass@123";

        login.enterEmail("unregistered_" + Instant.now().toEpochMilli() + "@visdum.test");
        login.enterPassword(testPassword);
        login.clickLogin();
        Assert.assertTrue(login.isAnyMessageVisible("email is invalid", "Invalid Credentials", "The selected email is invalid"),
                "Expected error message for unregistered email.");
    }
}
