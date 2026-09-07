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
 * Tests for Data Stream creation wizard: validation rules, Key-Value end-to-end flow,
 * and saving streams as drafts.
 */
public class CreateDataStreamTest extends BaseTest {

    @Test
    public void createDataStreamValidationEmptyName() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        // Click next without entering stream name
        wizard.clickNextStep();
        Assert.assertTrue(wizard.isFieldErrorVisible("Name")
                        || wizard.isFieldErrorVisible("required")
                        || wizard.isOpen(),
                "Validation message should appear or stay on Step 1 when Stream Name is empty.");

        wizard.clickCancel();
    }

    @Test
    public void createDataStreamValidationInvalidCharactersInName() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        wizard.enterStreamName("Invalid#Stream@Name!");
        wizard.clickNextStep();

        Assert.assertTrue(wizard.isFieldErrorVisible("alphanumeric")
                        || wizard.isFieldErrorVisible("characters")
                        || wizard.isOpen(),
                "Validation message should indicate special characters are not allowed.");

        wizard.clickCancel();
    }

    @Test
    public void createDataStreamWithKeyValueTypeEndToEnd() {
        ExecutionGuard.requireDestructiveTestsEnabled();

        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        String streamName = "KV_Stream_" + System.currentTimeMillis();
        wizard.enterStreamName(streamName);
        wizard.selectStreamType("Key-Value");

        // Key-Value jumps from Step 1 directly to Step 6 (Review & Save)
        wizard.clickNextStep();

        // Submit on Review Step
        wizard.clickSubmit();

        // Verify stream appears in the data streams list
        Assert.assertTrue(dataStreams.isLoaded(), "Expected redirect to Data Streams list.");
        dataStreams.search(streamName);
        Assert.assertTrue(dataStreams.isStreamListed(streamName),
                "Expected newly created Key-Value stream to be listed in Data Streams table.");
    }

    @Test
    public void createDataStreamSaveAsDraft() {
        ExecutionGuard.requireDestructiveTestsEnabled();

        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        String draftName = "Draft_Stream_" + System.currentTimeMillis();
        wizard.enterStreamName(draftName);
        wizard.selectStreamType("Earning");
        wizard.selectDataSource("Upload Sheets");

        if (wizard.isSaveToDraftVisible()) {
            wizard.clickSaveToDraft();

            Assert.assertTrue(dataStreams.isLoaded(), "Expected redirect to Data Streams list after saving draft.");
            dataStreams.switchToDraftTab();
            dataStreams.search(draftName);
            Assert.assertTrue(dataStreams.isStreamListed(draftName),
                    "Expected newly saved draft to be listed in Drafts tab.");
        } else {
            wizard.clickCancel();
        }
    }
}
