package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.CreateUserPage;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UserValidationTest extends BaseTest {

    @Test
    public void requiredFieldsAreValidatedOnFirstStep() {
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        CreateUserPage form = new CreateUserPage(DriverFactory.getDriver());
        users.openCreateUser();
        form.clickNextStep();
        Assert.assertTrue(form.isValidationMessageVisible("Name is required"), "Expected Name validation error.");
        Assert.assertTrue(form.isValidationMessageVisible("Email is required"), "Expected Email validation error.");
        Assert.assertTrue(form.isValidationMessageVisible("User Reference ID is required"), "Expected User Ref ID validation error.");
        Assert.assertTrue(form.isValidationMessageVisible("Role is required"), "Expected Role validation error.");
        Assert.assertTrue(form.isValidationMessageVisible("Currency is required"), "Expected Currency validation error.");
    }

    @Test
    public void invalidEmailFormatValidation() {
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        CreateUserPage form = new CreateUserPage(DriverFactory.getDriver());
        users.openCreateUser();

        form.enterName("Format Test User");
        form.enterEmail("not-a-valid-email");
        form.clickNextStep();

        Assert.assertTrue(form.isValidationMessageVisible("Wrong email format")
                        || form.isValidationMessageVisible("valid email")
                        || form.isValidationMessageVisible("email"),
                "Expected email format validation message.");
    }

    @Test
    public void passwordMismatchValidation() {
        long id = System.currentTimeMillis();
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        CreateUserPage form = new CreateUserPage(DriverFactory.getDriver());
        users.openCreateUser();

        // Step 1
        form.enterPersonalDetails("Mismatch User " + id, "mismatch." + id + "@example.test",
                "MM-" + id, System.getProperty("test.user.role", "Individual Contributor"),
                System.getProperty("test.user.currency", "INR"));
        form.clickNextStep();

        // Step 2
        form.clickNextStep();

        // Step 3
        form.enterPasswordDetails("Password@123");
        // Re-enter mismatched confirm password directly
        form.enterPasswordDetails("Password@123", false);
        try {
            org.openqa.selenium.WebElement confirmInput = DriverFactory.getDriver().findElement(org.openqa.selenium.By.name("confirm_password"));
            confirmInput.clear();
            confirmInput.sendKeys("DifferentPass@456");
            form.submitNewUser();
        } catch (Exception ignored) {
        }

        Assert.assertTrue(form.isValidationMessageVisible("Passwords must match")
                        || form.isValidationMessageVisible("match"),
                "Expected Passwords must match validation error.");
    }
}
