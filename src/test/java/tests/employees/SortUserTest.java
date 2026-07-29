package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.UserColumn;

/**
 * Covers AG-Grid column sorting on every sortable column defined in {@link UserColumn} (mirrored
 * from table/_columns.tsx, which sets {@code sortable: true} on every data column except the
 * row-number "S No" and the "Actions" column). Default AG-Grid header-click behavior applies since
 * no custom header component intercepts the click.
 */
public class SortUserTest extends BaseTest {

    @DataProvider(name = "sortableColumns")
    public Object[][] sortableColumns() {
        UserColumn[] columns = UserColumn.values();
        Object[][] data = new Object[columns.length][1];
        for (int i = 0; i < columns.length; i++) {
            data[i][0] = columns[i];
        }
        return data;
    }

    @Test(dataProvider = "sortableColumns")
    public void sortingColumnTogglesAriaSort(UserColumn column) {
        UsersPage users = new UsersPage(DriverFactory.getDriver());

        Assert.assertEquals(users.sortDirectionOf(column.colId), "none",
                column.headerName + " should start unsorted.");

        users.sortByColumn(column.colId);
        Assert.assertEquals(users.sortDirectionOf(column.colId), "ascending",
                column.headerName + " should sort ascending on first click.");

        users.sortByColumn(column.colId);
        Assert.assertEquals(users.sortDirectionOf(column.colId), "descending",
                column.headerName + " should sort descending on second click.");
    }
}
