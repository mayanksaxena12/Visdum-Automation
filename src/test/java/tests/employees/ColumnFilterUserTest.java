package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.UserColumn;

/**
 * Covers AG-Grid's built-in per-column "Set Filter" menu -- the &#8801; icon that appears on hover
 * over each header (Name, E-mail, Role, User Ref Id, Status, etc., as seen in the live Users grid
 * UI). This is distinct from the custom "Filter" side drawer opened by the funnel icon, which
 * {@link FilterUserTest} already covers via {@code Pages.UserFilterPage}.
 *
 * <p>table/_columns.tsx does not set {@code filterParams.buttons}, so this Set Filter has no
 * separate Apply/Reset button -- it filters live as each checkbox is toggled. This test toggles the
 * first available value off (applying a filter), then toggles the same value back on (clearing it)
 * so the filter doesn't leak into other tests. Filter values themselves are fetched from the
 * backend at runtime, so this only pins the column, not a specific value.
 */
public class ColumnFilterUserTest extends BaseTest {

    @DataProvider(name = "filterableColumns")
    public Object[][] filterableColumns() {
        UserColumn[] columns = UserColumn.values();
        Object[][] data = new Object[columns.length][1];
        for (int i = 0; i < columns.length; i++) {
            data[i][0] = columns[i];
        }
        return data;
    }

    @Test(dataProvider = "filterableColumns")
    public void columnMenuFilterTogglesLive(UserColumn column) {
        UsersPage users = new UsersPage(DriverFactory.getDriver());

        users.openColumnMenu(column.colId);
        Assert.assertTrue(users.isColumnMenuOpen(),
                "Expected the " + column.headerName + " column menu to open.");

        users.toggleFirstColumnFilterValue();
        users.closeColumnMenu();

        // Re-open and toggle the same value back on so this column's filter doesn't affect a
        // subsequent test run.
        users.openColumnMenu(column.colId);
        users.toggleFirstColumnFilterValue();
        users.closeColumnMenu();
    }
}
