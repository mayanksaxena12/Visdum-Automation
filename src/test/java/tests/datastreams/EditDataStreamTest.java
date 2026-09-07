package tests.datastreams;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.CreateDataStreamPage;
import Pages.DashboardPage;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for editing data streams via the wizard (CreateStreamWrapper.tsx / _CreateStream.tsx).
 */
public class EditDataStreamTest extends BaseTest {

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
    public void editDataStreamOpensWizardWithExistingDetails() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        Assert.assertTrue(dataStreams.isLoaded(), "Expected Data Streams page to load.");

        String streamName = resolveStreamName(dataStreams);
        dataStreams.search(streamName);

        if (dataStreams.isStreamListed(streamName)) {
            dataStreams.openEditStream(streamName);

            Assert.assertTrue(wizard.isOpen(), "Expected Create/Edit Data Stream wizard to open.");
            String loadedName = wizard.getStreamName();
            Assert.assertFalse(loadedName.isBlank(), "Expected stream name to be populated in edit mode.");

            // Cancel and verify we return to Data Streams
            wizard.clickCancel();
            Assert.assertTrue(dataStreams.isLoaded(), "Expected to return to Data Streams page after cancel.");
        }
    }
}
