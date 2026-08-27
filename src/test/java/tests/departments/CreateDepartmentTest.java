package tests.departments;

import java.time.Instant;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentFormPage;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

public class CreateDepartmentTest extends DepartmentsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireCreatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void createValidDepartment() {
        String id = String.valueOf(Instant.now().toEpochMilli());
        String departmentName = "Automation Dept " + id;

        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());
        DepartmentFormPage form = new DepartmentFormPage(DriverFactory.getDriver());

        departments.openCreateDepartment();
        form.enterDepartmentName(departmentName);
        form.enterDepartmentDescription("Created by Selenium automation");
        form.submitNewDepartment();

        departments.search(departmentName);
        Assert.assertTrue(departments.isRowListed(departmentName));
    }
}
