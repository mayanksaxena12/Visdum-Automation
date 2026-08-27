package tests.departments;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SearchDepartmentTest extends DepartmentsBaseTest {

    @Test
    public void verifySearchDepartment() {
        DepartmentsPage page = new DepartmentsPage(DriverFactory.getDriver());
        page.search("Sales");
         Assert.assertTrue(page.isRowListed("Sales"),
                "Expected a department row to match 'Sales'.");
    }
}
