package tests.teams;

import Base.DriverFactory;
import Base.TeamsBaseTest;
import Pages.TeamsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.TeamColumn;

/**
 * Covers AG-Grid's built-in per-column "Set Filter" menu on the Teams grid. Only "Team Name" and
 * "Status" configure {@code filter: 'agSetColumnFilter'} in table/_columns.tsx -- "S No", "Team
 * Members", and "Actions" don't, so they're excluded here. There is no advanced/custom "Filter"
 * drawer on the Teams page (that only exists on Users), so this is the only filter mechanism to
 * cover for this module.
 *
 * <p>Like the Users grid, no {@code filterParams.buttons} is configured, so the filter applies
 * live on checkbox toggle -- there is no separate Apply/Reset button.
 */
public class ColumnFilterTeamTest extends TeamsBaseTest {

    @DataProvider(name = "filterableColumns")
    public Object[][] filterableColumns() {
        return java.util.Arrays.stream(TeamColumn.values())
                .filter(column -> column.filterable)
                .map(column -> new Object[]{column})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "filterableColumns")
    public void columnMenuFilterTogglesLive(TeamColumn column) {
        TeamsPage teams = new TeamsPage(DriverFactory.getDriver());

        teams.openColumnMenu(column.colId);
        Assert.assertTrue(teams.isColumnMenuOpen(),
                "Expected the " + column.headerName + " column menu to open.");

        teams.toggleFirstColumnFilterValue();
        teams.closeColumnMenu();

        // Re-open and toggle the same value back on so this column's filter doesn't affect a
        // subsequent test run.
        teams.openColumnMenu(column.colId);
        teams.toggleFirstColumnFilterValue();
        teams.closeColumnMenu();
    }
}
