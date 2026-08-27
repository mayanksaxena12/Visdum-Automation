package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import Pages.UsersPage;

public class SearchUserTest extends BaseTest {

    @Test

    public void verifySearchUser() {

        // EmployeesPage page =
        //         new EmployeesPage(
        //                 DriverFactory.getDriver());
        UsersPage page = new UsersPage(DriverFactory.getDriver());

        page.search("Mayank");

        // Assert.assertTrue(
        //         DriverFactory.getDriver()
        //                 .getPageSource()
        //                 .contains("Mayank"));
        Assert.assertTrue(page.isRowListed("Mayank"),
                "Expected a user row to match 'Mayank'.");
        }   

}