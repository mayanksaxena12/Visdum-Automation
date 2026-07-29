package tests.departments;

import Base.DepartmentsBaseTest;
import Base.DriverFactory;
import Pages.DepartmentsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.DepartmentColumn;

/**
 * AG-Grid column sorting on the Departments grid. Only "Department Name" and "Status" are sortable
 * per table/_columns.ts (S No, Department Members and Actions are not), so this runs over those two.
 */
public class SortDepartmentTest extends DepartmentsBaseTest {

    @DataProvider(name = "sortableColumns")
    public Object[][] sortableColumns() {
        return java.util.Arrays.stream(DepartmentColumn.values())
                .filter(column -> column.sortable)
                .map(column -> new Object[]{column})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "sortableColumns")
    public void sortingColumnTogglesAriaSort(DepartmentColumn column) {
        DepartmentsPage departments = new DepartmentsPage(DriverFactory.getDriver());

        Assert.assertEquals(departments.sortDirectionOf(column.colId), "none",
                column.headerName + " should start unsorted.");

        departments.sortByColumn(column.colId);
        Assert.assertEquals(departments.sortDirectionOf(column.colId), "ascending",
                column.headerName + " should sort ascending on first click.");

        departments.sortByColumn(column.colId);
        Assert.assertEquals(departments.sortDirectionOf(column.colId), "descending",
                column.headerName + " should sort descending on second click.");
    }
}
