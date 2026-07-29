package tests.departments;

import java.time.Instant;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentFormPage;
import Pages.DepartmentsPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/**
 * Duplicate department-name validation. DepartmentForm.tsx surfaces the backend error
 * "The name has already been taken" when a duplicate name is submitted; the department is not
 * created. Requires an existing department name via -Dtest.department.existing.
 */
public class DuplicateDepartmentTest extends DepartmentsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireCreatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void duplicateNameIsRejected() {
        String existing = System.getProperty("test.department.existing", "");
        if (existing.isBlank()) {
            throw new IllegalArgumentException(
                    "Set -Dtest.department.existing=<existing department name> for the duplicate test.");
        }
        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());
        DepartmentFormPage form = new DepartmentFormPage(DriverFactory.getDriver());

        departments.openCreateDepartment();
        form.enterDepartmentName(existing);
        form.enterDepartmentDescription("Duplicate attempt " + Instant.now().toEpochMilli());
        form.submitNewDepartment();

        // The create drawer should remain open (submission blocked) rather than closing on success.
        // A duplicate name is not added to the list.
        org.testng.Assert.assertFalse(
                DriverFactory.getDriver().findElements(
                        org.openqa.selenium.By.xpath("//button[normalize-space()='Create']")).isEmpty(),
                "Create drawer should stay open because the duplicate name was rejected.");
    }
}
