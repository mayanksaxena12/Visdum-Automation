package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.UsersPage;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

public class ChangeUserPasswordTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requirePasswordChangePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void changeUserPassword() {
        String user = System.getProperty("test.user.existing", "");
        if (user.isBlank()) {
            throw new IllegalArgumentException("Set -Dtest.user.existing=<email or name> for the password test.");
        }
        String password = System.getProperty("test.user.password", "Test@1234");
        UsersPage users = new UsersPage(DriverFactory.getDriver());
        users.search(user);
        users.openChangePassword(user);
        DriverFactory.getDriver().findElement(By.name("new_password")).sendKeys(password);
        DriverFactory.getDriver().findElement(By.name("confirm_password")).sendKeys(password);
        DriverFactory.getDriver().findElement(By.xpath("//button[normalize-space()='Update']")).click();
    }
}
