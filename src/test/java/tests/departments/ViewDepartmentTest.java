package tests.departments;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentViewPage;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Covers the read-only "View" drawer (DepartmentActionModals/DepartmentView.tsx). */
public class ViewDepartmentTest extends DepartmentsBaseTest {

    @Test
    public void viewDepartmentShowsSearchedRecord() {
        String department = System.getProperty("test.department.existing", "Sales");

        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());
        DepartmentViewPage view = new DepartmentViewPage(DriverFactory.getDriver());

        departments.search(department);
        departments.openView(department);

        Assert.assertTrue(view.isOpen(), "Expected the department view drawer to open.");
        Assert.assertTrue(view.getName().toLowerCase().contains(department.toLowerCase()),
                "Expected the drawer's Department Name to contain the searched value.");

        view.close();
    }
}
