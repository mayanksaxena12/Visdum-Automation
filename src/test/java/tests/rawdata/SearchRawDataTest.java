package tests.rawdata;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.RawDataPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for loading and searching within Raw Data (RawDataWrapper.tsx / RawDataTable.tsx).
 */
public class SearchRawDataTest extends BaseTest {

    @Test
    public void verifyRawDataPageLoadAndSearch() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        String selectedTab = rawData.getSelectedDataStreamTab();
        Assert.assertFalse(selectedTab.isBlank(), "Expected a default data stream tab to be selected.");

        // Perform search
        String searchKeyword = System.getProperty("test.rawdata.search", "Deal");
        rawData.search(searchKeyword);

        Assert.assertTrue(rawData.isGridLoaded(), "Expected grid to be loaded after search.");
        rawData.clearSearch();
    }

    @Test
    public void verifySearchWithNoResults() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        rawData.search("non_existent_record_xyz_99999");
        Assert.assertFalse(rawData.isRowListed("non_existent_record_xyz_99999"),
                "Expected non-existent record to not be found in grid.");

        rawData.clearSearch();
    }
}
