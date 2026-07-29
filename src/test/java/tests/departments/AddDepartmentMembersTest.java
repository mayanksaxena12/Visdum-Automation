package tests.departments;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.AddTeamMembersModal;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/**
 * Covers the "Add Members" flow for Departments. DepartmentsList.tsx reuses the exact same
 * user-assign modal component as Teams ({@code teams/components/UserAssignModal} with
 * module='DEPARTMENT'), so the existing {@link AddTeamMembersModal} Page Object is reused here
 * rather than duplicating identical locators.
 */
public class AddDepartmentMembersTest extends DepartmentsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireAddMembersPermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void addFirstAvailableUserToDepartment() {
        String department = System.getProperty("test.department.existing", "");
        if (department.isBlank()) {
            throw new IllegalArgumentException(
                    "Set -Dtest.department.existing=<department name> for the add-members test.");
        }
        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());
        AddTeamMembersModal modal = new AddTeamMembersModal(DriverFactory.getDriver());

        departments.search(department);
        departments.openAddMembers(department);

        Assert.assertTrue(modal.isOpen(), "Expected the Add Members modal to open.");
        modal.selectFirstAvailableUser();
        modal.confirmAdd();
    }
}
