package tests.departments;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentFormPage;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

public class EditDepartmentTest extends DepartmentsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireEditPermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void editDepartmentName() {
        String department = System.getProperty("test.department.existing", "");
        if (department.isBlank()) {
            throw new IllegalArgumentException(
                    "Set -Dtest.department.existing=<department name> for the edit test.");
        }
        String updatedName =
                System.getProperty("test.department.updated.name", "Updated Automation Dept");
        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());
        DepartmentFormPage form = new DepartmentFormPage(DriverFactory.getDriver());

        departments.search(department);
        departments.openEditDepartment(department);
        // form.enterDepartmentName(
        //         System.getProperty("test.department.updated.name", "Updated Automation Dept"));
        form.enterDepartmentName(updatedName);
        form.saveEditedDepartment();

        departments.search(updatedName);
        Assert.assertTrue(departments.isRowListed(updatedName),
                "Expected the edited department '" + updatedName + "' to be listed.");
    }
}
