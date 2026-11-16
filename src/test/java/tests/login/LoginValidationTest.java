package tests.login;

import Base.DriverFactory;
import Base.LoginBaseTest;
import Pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ConfigReader;
import utilities.ExcelTestDataReader;

import java.util.Map;

/**
 * Excel-Driven Login Form Validation Test Suite.
 * Reads test inputs and invalid/boundary data strictly from manual-testcases.xlsx (Sheet: Login).
 */
public class LoginValidationTest extends LoginBaseTest {

    @Test(description = "Verify both fields empty shows validation errors")
    public void emptyFieldsShowRequiredErrors() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Email is required"));
        Assert.assertTrue(login.isFieldErrorVisible("Password is required"));
    }

    @Test(description = "TC_007: Verify login with only email filled shows Password is required")
    public void onlyEmailShowsPasswordRequired() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.enterEmail(ConfigReader.get("username"));
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Password is required"));
    }

    @Test(description = "TC_008: Verify login with only password filled shows Email is required")
    public void onlyPasswordShowsEmailRequired() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        Map<String, String> data = ExcelTestDataReader.getTestDataMap("Login", "TC_011");
        String testPassword = data.getOrDefault("Password", "TestPassword@123");

        login.enterPassword(testPassword);
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Email is required"));
    }

    @Test(description = "TC_009: Verify invalid email format shows error driven from Excel")
    public void invalidEmailFormatShowsError() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        String invalidEmail = ExcelTestDataReader.getRawValue("Login", "TC_009", "Test Data");
        Map<String, String> data = ExcelTestDataReader.getTestDataMap("Login", "TC_011");
        String testPassword = data.getOrDefault("Password", "TestPassword@123");

        login.enterEmail(invalidEmail);
        login.enterPassword(testPassword);
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Wrong email format"));
    }

    @Test(description = "TC_011: Verify email exceeding max length shows error driven from Excel")
    public void emailOver50CharsShowsMaxLengthError() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        Map<String, String> data = ExcelTestDataReader.getTestDataMap("Login", "TC_011");
        String longEmail = data.getOrDefault("Email", "a".repeat(55) + "@example.com");
        String testPassword = data.getOrDefault("Password", "TestPassword@123");

        login.enterEmail(longEmail);
        login.enterPassword(testPassword);
        login.clickLogin();
        Assert.assertTrue(login.isAnyFieldErrorVisible("Maximum 50 symbols", "Wrong email format"),
                "Expected either 'Maximum 50 symbols' or 'Wrong email format' validation alert.");
    }

    @Test(description = "TC_014: Verify password under 8 chars shows min length error driven from Excel")
    public void passwordUnder8CharsShowsMinLengthError() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        Map<String, String> data = ExcelTestDataReader.getTestDataMap("Login", "TC_014");
        String shortPassword = data.getOrDefault("Password", "cad");
        if (shortPassword.contains(" ")) {
            shortPassword = shortPassword.split(" ")[0]; // clean up any comments like "cad (3 characters)"
        }

        login.enterEmail(ConfigReader.get("username"));
        login.enterPassword(shortPassword);
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Minimum 8 symbols"));
    }
}
