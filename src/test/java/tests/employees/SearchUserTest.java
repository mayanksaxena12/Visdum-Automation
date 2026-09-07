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

        String searchTerm = System.getProperty("search.user", "");
        if (searchTerm.isBlank()) {
            try {
                searchTerm = page.getFirstRowValue("name");
            } catch (Exception ignored) {
            }
        }
        if (searchTerm.isBlank()) {
            searchTerm = "Mayank";
        }
        page.search(searchTerm);
        Assert.assertTrue(page.isRowListed(searchTerm),
                "Expected a user row to match '" + searchTerm + "'.");
    }   

}