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
 * Tests for Earning type Data Streams across all three formats:
 * Connected Apps, Manual Sheets, and Base Views.
 */
public class EarningStreamTest extends BaseTest {

    @Test
    public void createEarningStreamWithConnectedAppFormat() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();
        Assert.assertTrue(wizard.isOpen(), "Expected Create Data Stream wizard to open.");

        String streamName = "Earning_App_" + System.currentTimeMillis();
        wizard.enterStreamName(streamName);
        wizard.selectStreamType("Earning");

        wizard.selectDataSource("Connected Apps");
        Assert.assertTrue(wizard.isAppSelectVisible() || wizard.isDataSourceSelectionVisible(),
                "Expected Connected App controls to be displayed for Connected Apps format.");

        wizard.setAutoProcessDealCredits(true);
        wizard.setFetchOnlyNewRecords(true);

        if (ExecutionGuard.isDestructiveTestsEnabled() && wizard.isSaveToDraftVisible()) {
            wizard.clickSaveToDraft();
            Assert.assertTrue(dataStreams.isLoaded(), "Expected redirect to Data Streams list.");
            dataStreams.switchToDraftTab();
            Assert.assertTrue(dataStreams.isStreamListed(streamName),
                    "Expected Earning draft stream to be listed in Drafts tab.");
        } else {
            wizard.clickCancel();
        }
    }

    @Test
    public void createEarningStreamWithManualSheetFormat() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        String streamName = "Earning_Manual_" + System.currentTimeMillis();
        wizard.enterStreamName(streamName);
        wizard.selectStreamType("Earning");

        wizard.selectDataSource("Upload Sheets");
        Assert.assertTrue(wizard.isDropzoneVisible(),
                "Expected manual sheet dropzone to be visible for Upload Sheets format.");

        wizard.setAutoProcessDealCredits(false);
        wizard.setFetchOnlyNewRecords(false);

        if (ExecutionGuard.isDestructiveTestsEnabled() && wizard.isSaveToDraftVisible()) {
            wizard.clickSaveToDraft();
            Assert.assertTrue(dataStreams.isLoaded(), "Expected redirect to Data Streams list.");
            dataStreams.switchToDraftTab();
            Assert.assertTrue(dataStreams.isStreamListed(streamName),
                    "Expected Earning manual draft stream to be listed in Drafts tab.");
        } else {
            wizard.clickCancel();
        }
    }

    @Test
    public void createEarningStreamWithBaseViewFormat() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        String streamName = "Earning_BaseView_" + System.currentTimeMillis();
        wizard.enterStreamName(streamName);
        wizard.selectStreamType("Earning");

        wizard.selectDataSource("Base Views");
        Assert.assertTrue(wizard.isBaseViewSelectVisible(),
                "Expected Base View dropdown to be visible for Base Views format.");

        wizard.setAutoProcessDealCredits(true);

        if (ExecutionGuard.isDestructiveTestsEnabled() && wizard.isSaveToDraftVisible()) {
            wizard.clickSaveToDraft();
            Assert.assertTrue(dataStreams.isLoaded(), "Expected redirect to Data Streams list.");
            dataStreams.switchToDraftTab();
            Assert.assertTrue(dataStreams.isStreamListed(streamName),
                    "Expected Earning Base View draft stream to be listed in Drafts tab.");
        } else {
            wizard.clickCancel();
        }
    }
}
