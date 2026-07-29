package tests.departments;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentStatusModal;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/** Deactivates a currently-Active department (confirmation prompt "Are you sure you want to deactivate?"). */
public class DeactivateDepartmentTest extends DepartmentsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireDeactivatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void deactivateActiveDepartment() {
        String department = System.getProperty("test.department.existing", "");
        if (department.isBlank()) {
            throw new IllegalArgumentException(
                    "Set -Dtest.department.existing=<department name> for the deactivate test.");
        }
        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());
        DepartmentStatusModal modal = new DepartmentStatusModal(DriverFactory.getDriver());

        departments.search(department);
        Assert.assertEquals(departments.statusOf(department), "Active",
                "This test deactivates a department, so it must start Active.");

        departments.openToggleStatus(department);
        Assert.assertTrue(modal.isConfirmationVisible("Are you sure you want to deactivate?"));
        modal.confirm();

        departments.search(department);
        Assert.assertEquals(departments.statusOf(department), "Inactive");
    }
}
