package tests.login;

import Base.DriverFactory;
import Base.LoginBaseTest;
import Pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ConfigReader;

/**
 * Login field-validation coverage (Yup schema in Login.tsx). All read-only -- no OTP required.
 */
public class LoginValidationTest extends LoginBaseTest {

    @Test
    public void emptyFieldsShowBothRequiredErrors() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Email is required"));
        Assert.assertTrue(login.isFieldErrorVisible("Password is required"));
    }

    @Test
    public void onlyEmailShowsPasswordRequired() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.enterEmail(ConfigReader.get("username"));
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Password is required"));
    }

    @Test
    public void onlyPasswordShowsEmailRequired() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.enterPassword("SomePass@123");
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Email is required"));
    }

    @Test
    public void invalidEmailFormatShowsError() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.enterEmail("email@");
        login.enterPassword("SomePass@123");
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Wrong email format"));
    }

    @Test
    public void emailOver50CharsShowsMaxLengthError() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.enterEmail("a".repeat(45) + "@example.com"); // 57 chars, > 50
        login.enterPassword("SomePass@123");
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Maximum 50 symbols"));
    }

    @Test
    public void passwordUnder8CharsShowsMinLengthError() {
        LoginPage login = new LoginPage(DriverFactory.getDriver());
        login.enterEmail(ConfigReader.get("username"));
        login.enterPassword("Ab@1");
        login.clickLogin();
        Assert.assertTrue(login.isFieldErrorVisible("Minimum 8 symbols"));
    }
}
