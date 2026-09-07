package tests.datastreams;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/**
 * Tests for Data Stream status transitions (Activate, Deactivate, and Delete Draft).
 * Includes non-destructive modal cancellation tests and guarded state changes.
 */
public class StreamStatusTest extends BaseTest {

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
    public void verifyDeactivateModalCanBeCancelled() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        Assert.assertTrue(dataStreams.isLoaded(), "Expected Data Streams page to load.");

        String streamName = resolveStreamName(dataStreams);
        dataStreams.search(streamName);

        if (dataStreams.isStreamListed(streamName)) {
            String initialStatus = dataStreams.statusOf(streamName);
            if ("Active".equalsIgnoreCase(initialStatus)) {
                dataStreams.openDeactivateStream(streamName);
                Assert.assertTrue(dataStreams.isStatusChangeModalOpen(),
                        "Expected Deactivate confirmation modal to appear.");

                dataStreams.cancelStatusChange();
                Assert.assertFalse(dataStreams.isStatusChangeModalOpen(),
                        "Expected Deactivate confirmation modal to close on Cancel.");
                Assert.assertEquals(dataStreams.statusOf(streamName), initialStatus,
                        "Stream status should remain unchanged after cancelling modal.");
            }
        }
    }

    @Test
    public void verifyDeleteDraftModalCanBeCancelled() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        Assert.assertTrue(dataStreams.isLoaded(), "Expected Data Streams page to load.");

        dataStreams.switchToDraftTab();
        Assert.assertTrue(dataStreams.isDraftTabSelected(), "Expected Drafts tab to be selected.");

        try {
            String draftName = dataStreams.getFirstRowValue("name");
            if (!draftName.isBlank()) {
                dataStreams.openDeleteDraft(draftName);
                Assert.assertTrue(dataStreams.isDeleteDraftModalOpen(),
                        "Expected Delete Draft confirmation modal to appear.");

                dataStreams.cancelDeleteDraft();
                Assert.assertFalse(dataStreams.isDeleteDraftModalOpen(),
                        "Expected Delete Draft confirmation modal to close on Cancel.");
                Assert.assertTrue(dataStreams.isStreamListed(draftName),
                        "Draft stream should still be listed after cancelling delete.");
            }
        } catch (Exception ignored) {
            // If no drafts exist in the environment, test completes gracefully
        }
    }

    @Test
    public void deactivateAndReactivateStream() {
        ExecutionGuard.requireDestructiveTestsEnabled();

        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        String streamName = resolveStreamName(dataStreams);
        dataStreams.search(streamName);

        if (dataStreams.isStreamListed(streamName)) {
            // Deactivate
            dataStreams.openDeactivateStream(streamName);
            dataStreams.confirmStatusChange();
            Assert.assertEquals(dataStreams.statusOf(streamName), "Inactive",
                    "Expected stream status to become Inactive.");

            // Reactivate
            dataStreams.openActivateStream(streamName);
            dataStreams.confirmStatusChange();
            Assert.assertEquals(dataStreams.statusOf(streamName), "Active",
                    "Expected stream status to become Active again.");
        }
    }
}
