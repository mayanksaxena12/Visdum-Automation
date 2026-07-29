package tests.teams;

import Base.DriverFactory;
import Base.TeamsBaseTest;
import Pages.TeamsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.TeamColumn;

/**
 * Covers AG-Grid column sorting on the Teams grid. Only "S No" (id), "Team Name", and "Status" are
 * sortable per table/_columns.tsx -- "Team Members" and "Actions" are not, so they're excluded here.
 */
public class SortTeamTest extends TeamsBaseTest {

    @DataProvider(name = "sortableColumns")
    public Object[][] sortableColumns() {
        return java.util.Arrays.stream(TeamColumn.values())
                .filter(column -> column.sortable)
                .map(column -> new Object[]{column})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "sortableColumns")
    public void sortingColumnTogglesAriaSort(TeamColumn column) {
        TeamsPage teams = new TeamsPage(DriverFactory.getDriver());

        Assert.assertEquals(teams.sortDirectionOf(column.colId), "none",
                column.headerName + " should start unsorted.");

        teams.sortByColumn(column.colId);
        Assert.assertEquals(teams.sortDirectionOf(column.colId), "ascending",
                column.headerName + " should sort ascending on first click.");

        teams.sortByColumn(column.colId);
        Assert.assertEquals(teams.sortDirectionOf(column.colId), "descending",
                column.headerName + " should sort descending on second click.");
    }
}
