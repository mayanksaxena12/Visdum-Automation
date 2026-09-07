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
 * Tests for Reference type Data Streams across all supported formats:
 * Connected Apps, Manual Sheets, and Base Views.
 * Also verifies Reference-specific business logic (e.g. Deal Credits radio is absent).
 */
public class ReferenceStreamTest extends BaseTest {

    @Test
    public void createReferenceStreamWithConnectedAppFormat() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();
        Assert.assertTrue(wizard.isOpen(), "Expected Create Data Stream wizard to open.");

        String streamName = "Ref_App_" + System.currentTimeMillis();
        wizard.enterStreamName(streamName);
        wizard.selectStreamType("Reference");

        // Reference streams should NOT display the Deal Credits radio
        Assert.assertFalse(wizard.isDealCreditsRadioVisible(),
                "Reference streams must not display the automatic deal credits option.");

        wizard.selectDataSource("Connected Apps");
        Assert.assertTrue(wizard.isAppSelectVisible() || wizard.isDataSourceSelectionVisible(),
                "Expected Connected App controls to be displayed for Connected Apps format.");

        wizard.setFetchOnlyNewRecords(true);

        if (ExecutionGuard.isDestructiveTestsEnabled() && wizard.isSaveToDraftVisible()) {
            wizard.clickSaveToDraft();
            Assert.assertTrue(dataStreams.isLoaded(), "Expected redirect to Data Streams list.");
            dataStreams.switchToDraftTab();
            Assert.assertTrue(dataStreams.isStreamListed(streamName),
                    "Expected Reference Connected App draft stream to be listed in Drafts tab.");
        } else {
            wizard.clickCancel();
        }
    }

    @Test
    public void createReferenceStreamWithManualSheetFormat() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        String streamName = "Ref_Manual_" + System.currentTimeMillis();
        wizard.enterStreamName(streamName);
        wizard.selectStreamType("Reference");

        Assert.assertFalse(wizard.isDealCreditsRadioVisible(),
                "Reference streams must not display the automatic deal credits option.");

        wizard.selectDataSource("Upload Sheets");
        Assert.assertTrue(wizard.isDropzoneVisible(),
                "Expected manual sheet dropzone to be visible for Upload Sheets format.");

        wizard.setFetchOnlyNewRecords(false);

        if (ExecutionGuard.isDestructiveTestsEnabled() && wizard.isSaveToDraftVisible()) {
            wizard.clickSaveToDraft();
            Assert.assertTrue(dataStreams.isLoaded(), "Expected redirect to Data Streams list.");
            dataStreams.switchToDraftTab();
            Assert.assertTrue(dataStreams.isStreamListed(streamName),
                    "Expected Reference manual draft stream to be listed in Drafts tab.");
        } else {
            wizard.clickCancel();
        }
    }

    @Test
    public void createReferenceStreamWithBaseViewFormat() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        String streamName = "Ref_BaseView_" + System.currentTimeMillis();
        wizard.enterStreamName(streamName);
        wizard.selectStreamType("Reference");

        Assert.assertFalse(wizard.isDealCreditsRadioVisible(),
                "Reference streams must not display the automatic deal credits option.");

        wizard.selectDataSource("Base Views");
        Assert.assertTrue(wizard.isBaseViewSelectVisible(),
                "Expected Base View dropdown to be visible for Base Views format.");

        if (ExecutionGuard.isDestructiveTestsEnabled() && wizard.isSaveToDraftVisible()) {
            wizard.clickSaveToDraft();
            Assert.assertTrue(dataStreams.isLoaded(), "Expected redirect to Data Streams list.");
            dataStreams.switchToDraftTab();
            Assert.assertTrue(dataStreams.isStreamListed(streamName),
                    "Expected Reference Base View draft stream to be listed in Drafts tab.");
        } else {
            wizard.clickCancel();
        }
    }
}
