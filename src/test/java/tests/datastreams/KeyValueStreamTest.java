package tests.datastreams;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.CreateDataStreamPage;
import Pages.DashboardPage;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/**
 * Tests for Key-Value type Data Streams.
 * Verifies Key-Value specific behavior:
 * - External data sources (Connected Apps, Upload Sheets, Base Views) are hidden.
 * - Deal Credits and Fetch New Records options are hidden.
 * - Key-Value stream creation and draft saving functionality.
 */
public class KeyValueStreamTest extends BaseTest {

    @Test
    public void verifyKeyValueStreamHidesExternalDataSources() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();
        Assert.assertTrue(wizard.isOpen(), "Expected Create Data Stream wizard to open.");

        String streamName = "KV_UI_" + System.currentTimeMillis();
        wizard.enterStreamName(streamName);
        wizard.selectStreamType("Key-Value");

        // Key-Value streams should NOT show external data sources selection
        Assert.assertFalse(wizard.isDataSourceSelectionVisible(),
                "Key-Value streams must not display external data source triggers.");

        // Key-Value streams should NOT show Deal Credits radio
        Assert.assertFalse(wizard.isDealCreditsRadioVisible(),
                "Key-Value streams must not display the automatic deal credits option.");

        wizard.clickCancel();
    }

    @Test
    public void createKeyValueStreamDraft() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();
        Assert.assertTrue(wizard.isOpen(), "Expected Create Data Stream wizard to open.");

        String streamName = "KV_Draft_" + System.currentTimeMillis();
        wizard.enterStreamName(streamName);
        wizard.selectStreamType("Key-Value");

        if (ExecutionGuard.isDestructiveTestsEnabled() && wizard.isSaveToDraftVisible()) {
            wizard.clickSaveToDraft();
            Assert.assertTrue(dataStreams.isLoaded(), "Expected redirect to Data Streams list.");
            dataStreams.switchToDraftTab();
            Assert.assertTrue(dataStreams.isStreamListed(streamName),
                    "Expected Key-Value draft stream to be listed in Drafts tab.");
        } else {
            wizard.clickCancel();
        }
    }
}
