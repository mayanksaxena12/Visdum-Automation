package tests.datastreams;

import Base.DataStreamsBaseTest;
import Base.DriverFactory;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.DataStreamColumn;

/**
 * Covers AG-Grid's built-in per-column "Set Filter" menu on the Data Streams grid.
 * All 6 columns in {@link DataStreamColumn} (Stream Name, Source, Stream Status,
 * Stream Type, Department, Last Refreshed) configure agSetColumnFilter per table/_columns.ts.
 */
public class ColumnFilterDataStreamTest extends DataStreamsBaseTest {

    @DataProvider(name = "filterableColumns")
    public Object[][] filterableColumns() {
        return java.util.Arrays.stream(DataStreamColumn.values())
                .filter(column -> column.filterable)
                .map(column -> new Object[]{column})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "filterableColumns")
    public void columnMenuFilterTogglesLive(DataStreamColumn column) {
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());

        dataStreams.openColumnMenu(column.colId);
        Assert.assertTrue(dataStreams.isColumnMenuOpen(),
                "Expected the " + column.headerName + " column menu to open.");

        dataStreams.toggleFirstColumnFilterValue();
        dataStreams.closeColumnMenu();

        // Re-open and toggle the same value back on so this column's filter doesn't affect subsequent tests.
        dataStreams.openColumnMenu(column.colId);
        dataStreams.toggleFirstColumnFilterValue();
        dataStreams.closeColumnMenu();
    }
}