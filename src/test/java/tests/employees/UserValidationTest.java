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
        Assert.assertTrue(form.isValidationMessageVisible("Name is required"));
    }
}
