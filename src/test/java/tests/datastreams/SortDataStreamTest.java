package tests.datastreams;

import Base.DataStreamsBaseTest;
import Base.DriverFactory;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.DataStreamColumn;

/**
 * Covers AG-Grid column sorting on the Data Streams grid.
 * All 6 columns defined in {@link DataStreamColumn} (Stream Name, Source, Stream Status,
 * Stream Type, Department, Last Refreshed) are sortable per table/_columns.ts.
 */
public class SortDataStreamTest extends DataStreamsBaseTest {

    @DataProvider(name = "sortableColumns")
    public Object[][] sortableColumns() {
        return java.util.Arrays.stream(DataStreamColumn.values())
                .filter(column -> column.sortable)
                .map(column -> new Object[]{column})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "sortableColumns")
    public void sortingColumnTogglesAriaSort(DataStreamColumn column) {
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());

        Assert.assertEquals(dataStreams.sortDirectionOf(column.colId), "none",
                column.headerName + " should start unsorted.");

        dataStreams.sortByColumn(column.colId);
        Assert.assertEquals(dataStreams.sortDirectionOf(column.colId), "ascending",
                column.headerName + " should sort ascending on first click.");

        dataStreams.sortByColumn(column.colId);
        Assert.assertEquals(dataStreams.sortDirectionOf(column.colId), "descending",
                column.headerName + " should sort descending on second click.");
    }
}