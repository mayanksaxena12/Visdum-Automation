package tests.departments;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentStatusModal;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/** Activates a currently-Inactive department (confirmation prompt "Are you sure you want to activate?"). */
public class ActivateDepartmentTest extends DepartmentsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireActivatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void activateInactiveDepartment() {
        String department = System.getProperty("test.department.existing", "");
        if (department.isBlank()) {
            throw new IllegalArgumentException(
                    "Set -Dtest.department.existing=<department name> for the activate test.");
        }
        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());
        DepartmentStatusModal modal = new DepartmentStatusModal(DriverFactory.getDriver());

        departments.search(department);
        Assert.assertEquals(departments.statusOf(department), "Inactive",
                "This test activates a department, so it must start Inactive.");

        departments.openToggleStatus(department);
        Assert.assertTrue(modal.isConfirmationVisible("Are you sure you want to activate?"));
        modal.confirm();

        departments.search(department);
        Assert.assertEquals(departments.statusOf(department), "Active");
    }
}
