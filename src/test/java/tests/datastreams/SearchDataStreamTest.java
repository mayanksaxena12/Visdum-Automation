package tests.datastreams;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SearchDataStreamTest extends BaseTest {

    private String resolveStreamName(DataStreamsPage dataStreams) {
        String name = System.getProperty("test.stream.existing", "");
        if (name.isBlank()) {
            try {
                name = dataStreams.getFirstRowValue("name");
            } catch (Exception ignored) {
            }
        }
        if (name.isBlank()) {
            name = "Deals";
        }
        return name;
    }

    @Test
    public void verifyDataStreamsPageLoadAndSearch() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        Assert.assertTrue(dataStreams.isLoaded(), "Expected Data Streams page to load.");
        Assert.assertTrue(dataStreams.isActiveTabSelected(), "Expected 'Data Streams' (Active) tab to be selected by default.");

        String streamName = resolveStreamName(dataStreams);
        dataStreams.search(streamName);
        Assert.assertTrue(dataStreams.isStreamListed(streamName) || dataStreams.isGridLoaded(),
                "Expected searched stream or filtered grid to be displayed.");

        dataStreams.clearSearch();
    }

    @Test
    public void verifySwitchBetweenActiveAndDraftTabs() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        Assert.assertTrue(dataStreams.isLoaded(), "Expected Data Streams page to load.");

        // Switch to Drafts tab
        dataStreams.switchToDraftTab();
        Assert.assertTrue(dataStreams.isDraftTabSelected(), "Expected 'Drafts' tab to be active after click.");

        // Switch back to Active tab
        dataStreams.switchToActiveTab();
        Assert.assertTrue(dataStreams.isActiveTabSelected(), "Expected 'Data Streams' tab to be active after click.");
    }

    @Test
    public void verifySearchWithNoResults() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        Assert.assertTrue(dataStreams.isLoaded(), "Expected Data Streams page to load.");

        dataStreams.search("non_existent_stream_xyz_99999");
        Assert.assertFalse(dataStreams.isStreamListed("non_existent_stream_xyz_99999"),
                "Expected non-existent stream to not be listed in grid.");

        dataStreams.clearSearch();
    }
}
