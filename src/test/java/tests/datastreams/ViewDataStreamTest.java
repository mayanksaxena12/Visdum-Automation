package tests.datastreams;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.DataStreamViewPage;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the read-only Data Stream View drawer (DataStreamView.tsx / _StreamDetailView.tsx).
 */
public class ViewDataStreamTest extends BaseTest {

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
    public void viewDataStreamShowsDetailsInDrawer() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        DataStreamViewPage viewPage = new DataStreamViewPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        Assert.assertTrue(dataStreams.isLoaded(), "Expected Data Streams page to load.");

        String streamName = resolveStreamName(dataStreams);
        dataStreams.search(streamName);

        if (dataStreams.isStreamListed(streamName)) {
            dataStreams.openViewStream(streamName);

            Assert.assertTrue(viewPage.isOpen(), "Expected Data Stream view drawer to open.");
            String drawerStreamName = viewPage.getStreamName();
            Assert.assertFalse(drawerStreamName.isBlank(), "Expected stream name in drawer to not be blank.");
            Assert.assertTrue(drawerStreamName.toLowerCase().contains(streamName.toLowerCase())
                            || streamName.toLowerCase().contains(drawerStreamName.toLowerCase()),
                    "Expected drawer Stream Name to match searched stream name.");

            viewPage.close();
        }
    }
}
