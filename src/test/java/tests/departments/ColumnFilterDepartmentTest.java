package tests.departments;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.DepartmentColumn;

/**
 * AG-Grid's built-in per-column Set Filter menu on the Departments grid. Only "Department Name" and
 * "Status" are filterable per table/_columns.ts. Like Users/Teams, no {@code filterParams.buttons}
 * is configured, so filtering applies live on checkbox toggle (no Apply button).
 */
public class ColumnFilterDepartmentTest extends DepartmentsBaseTest {

    @DataProvider(name = "filterableColumns")
    public Object[][] filterableColumns() {
        return java.util.Arrays.stream(DepartmentColumn.values())
                .filter(column -> column.filterable)
                .map(column -> new Object[]{column})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "filterableColumns")
    public void columnMenuFilterTogglesLive(DepartmentColumn column) {
        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());

        departments.openColumnMenu(column.colId);
        Assert.assertTrue(departments.isColumnMenuOpen(),
                "Expected the " + column.headerName + " column menu to open.");

        departments.toggleFirstColumnFilterValue();
        departments.closeColumnMenu();

        departments.openColumnMenu(column.colId);
        departments.toggleFirstColumnFilterValue();
        departments.closeColumnMenu();
    }
}
