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

        String searchTerm = System.getProperty("search.user", "Mayank");
        page.search(searchTerm);
        Assert.assertTrue(page.isRowListed(searchTerm),
                "Expected a user row to match '" + searchTerm + "'.");
    }   

}