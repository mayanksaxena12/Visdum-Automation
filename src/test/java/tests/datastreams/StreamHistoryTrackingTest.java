package tests.datastreams;

import Base.DataStreamsBaseTest;
import Base.DriverFactory;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for opening and closing the "Set History Tracking" action drawer from the Data Streams grid.
 */
public class StreamHistoryTrackingTest extends DataStreamsBaseTest {

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
    public void openAndCloseHistoryTrackingDrawer() {
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        Assert.assertTrue(dataStreams.isLoaded(), "Expected Data Streams page to load.");

        String streamName = resolveStreamName(dataStreams);
        dataStreams.search(streamName);

        if (dataStreams.isStreamListed(streamName)) {
            String status = dataStreams.statusOf(streamName);
            if ("Active".equalsIgnoreCase(status)) {
                dataStreams.openSetHistoryTracking(streamName);
                Assert.assertTrue(dataStreams.isHistoryTrackingDrawerOpen(),
                        "Expected History Tracking drawer to open for stream: " + streamName);

                dataStreams.closeHistoryTrackingDrawer();
                Assert.assertFalse(dataStreams.isHistoryTrackingDrawerOpen(),
                        "Expected History Tracking drawer to close.");
            }
        }
    }
}