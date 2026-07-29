package tests.departments;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentFormPage;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Required-field + boundary validation on the Create Department form. */
public class DepartmentValidationTest extends DepartmentsBaseTest {

    @Test
    public void requiredFieldValidationOnSubmit() {
        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());
        DepartmentFormPage form = new DepartmentFormPage(DriverFactory.getDriver());

        departments.openCreateDepartment();
        form.submitNewDepartment();

        Assert.assertTrue(form.isValidationMessageVisible("Department Name is required"));
    }

    @Test
    public void minimumLengthValidation() {
        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());
        DepartmentFormPage form = new DepartmentFormPage(DriverFactory.getDriver());

        departments.openCreateDepartment();
        form.enterDepartmentName("Ab");   // below the 3-character minimum
        form.submitNewDepartment();

        Assert.assertTrue(form.isValidationMessageVisible("Minimum 3 symbols"));
    }
}
