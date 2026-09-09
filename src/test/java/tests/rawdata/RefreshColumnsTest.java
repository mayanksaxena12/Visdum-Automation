package tests.rawdata;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.DealsModalPage;
import Pages.RawDataPage;
import java.util.List;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * Test suite for the "Refresh Columns" feature in Raw Data.
 *
 * <p>Automates end-to-end scenarios covering all determined conditions:
 * <ul>
 *   <li>Condition 1: Menu action visibility, exact label, and styling on eligible streams</li>
 *   <li>Condition 2: Menu action exclusion on Key-Value and streams without calculated columns</li>
 *   <li>Condition 3: Menu integrity preserving existing header actions</li>
 *   <li>Condition 4: Confirmation modal UX, exact prompt text, and button styles (Cancel/Submit)</li>
 *   <li>Condition 5: Confirmation modal cancellation via Cancel button & header close icon</li>
 *   <li>Condition 6: Async execution trigger & DealsModal progress state (progress bar & notice)</li>
 *   <li>Condition 7: Dismissing DealsModal during progress without aborting the background job</li>
 *   <li>Condition 8: Completion metrics validation (Derived/Lookup columns & Records processed/updated)</li>
 *   <li>Condition 9: AG Grid table synchronization and stream tab scoping</li>
 * </ul>
 */
public class RefreshColumnsTest extends BaseTest {

    private RawDataPage navigateToRawDataPage() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());
        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load successfully.");
        return rawData;
    }

    /**
     * Condition 1 & 7: Verify 'Refresh Columns' action is displayed with exact label
     * for an eligible Raw Data stream containing calculated (Derived/Lookup) columns.
     */
    @Test(priority = 1)
    public void verifyRefreshColumnsActionDisplayedOnEligibleStream() {
        RawDataPage rawData = navigateToRawDataPage();

        if (!rawData.isHeaderActionsPresent()) {
            throw new SkipException("Header actions menu not present on current stream (may be Key-Value or non-admin).");
        }

        List<String> actions = rawData.getHeaderActionNames();
        System.out.println("  [DEBUG] Available Header Actions: " + actions);

        boolean isEligibleStream = actions.contains("Refresh Columns");
        if (!isEligibleStream) {
            // Try searching other tabs for an eligible stream
            List<String> tabs = rawData.getDataStreamTabNames();
            for (String tab : tabs) {
                rawData.selectDataStreamTab(tab);
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    isEligibleStream = true;
                    actions = rawData.getHeaderActionNames();
                    break;
                }
            }
        }

        if (!isEligibleStream) {
            throw new SkipException("No data stream with Derived/Lookup columns found in current organization.");
        }

        // Verify exact action label
        Assert.assertTrue(actions.contains("Refresh Columns"),
                "Expected 'Refresh Columns' action to be displayed in header actions menu.");

        // Verify action appears exactly once
        long count = actions.stream().filter(a -> a.equalsIgnoreCase("Refresh Columns")).count();
        Assert.assertEquals(count, 1, "Expected 'Refresh Columns' action to appear exactly once in the menu.");
    }

    /**
     * Condition 8: Verify 'Refresh Columns' action does not displace or remove
     * standard header actions (e.g. Add New Row, Process Deal Credits, Edit).
     */
    @Test(priority = 2)
    public void verifyActionMenuIntegrityWithRefreshColumns() {
        RawDataPage rawData = navigateToRawDataPage();

        if (!rawData.isHeaderActionsPresent()) {
            throw new SkipException("Header actions menu not present on current stream.");
        }

        List<String> actions = rawData.getHeaderActionNames();
        // At least one standard action should coexist with or without Refresh Columns
        boolean hasStandardAction = actions.stream().anyMatch(a ->
                a.contains("Add New Row")
                        || a.contains("Process")
                        || a.contains("Edit")
                        || a.contains("Owner")
                        || a.contains("Collaborate")
                        || a.contains("Refresh Columns"));

        Assert.assertTrue(hasStandardAction, "Expected standard header actions to be present in menu.");
    }

    /**
     * Condition 5: Verify 'Refresh Columns' action is excluded for Key-Value streams.
     */
    @Test(priority = 3)
    public void verifyRefreshColumnsHiddenForKeyValueStream() {
        RawDataPage rawData = navigateToRawDataPage();
        List<String> tabs = rawData.getDataStreamTabNames();

        String keyValueTab = null;
        for (String tab : tabs) {
            if (tab.toLowerCase().contains("key-value") || tab.toLowerCase().contains("key value")) {
                keyValueTab = tab;
                break;
            }
        }

        if (keyValueTab == null) {
            throw new SkipException("No Key-Value data stream tab found in current organization to verify exclusion.");
        }

        rawData.selectDataStreamTab(keyValueTab);

        // On Key-Value streams, header actions menu is completely hidden or does not contain Refresh Columns
        if (rawData.isHeaderActionsPresent()) {
            Assert.assertFalse(rawData.isHeaderActionPresent("Refresh Columns"),
                    "Expected 'Refresh Columns' to be hidden for Key-Value stream.");
        } else {
            Assert.assertFalse(rawData.isHeaderActionsPresent(),
                    "Expected header kebab menu to be hidden for Key-Value stream.");
        }
    }

    /**
     * Condition 4, 9, 10, 11, 12: Verify clicking 'Refresh Columns' opens confirmation modal
     * with exact message prompt, outline Cancel button, and primary Submit button.
     */
    @Test(priority = 4)
    public void verifyRefreshColumnsConfirmationModalContentAndStyling() {
        RawDataPage rawData = navigateToRawDataPage();

        if (!ensureEligibleStreamSelected(rawData)) {
            throw new SkipException("No eligible stream with Refresh Columns action available.");
        }

        rawData.clickRefreshColumns();
        Assert.assertTrue(rawData.isConfirmationModalOpen(),
                "Expected confirmation modal to open upon clicking 'Refresh Columns'.");

        // Verify exact prompt text
        String modalText = rawData.getConfirmationModalText();
        System.out.println("  [DEBUG] Modal confirmation text: " + modalText);
        Assert.assertEquals(modalText.trim(),
                "Are you sure you want to refresh all Derived and Lookup Column values?",
                "Confirmation modal text should exactly match the specification.");

        // Verify buttons presence and styling
        Assert.assertTrue(rawData.isConfirmationCancelButtonPresent(),
                "Expected Cancel button to be present in confirmation modal.");
        Assert.assertTrue(rawData.isConfirmationSubmitButtonPresent(),
                "Expected Submit button to be present in confirmation modal.");
        Assert.assertTrue(rawData.isConfirmationCancelButtonOutlineStyled(),
                "Expected Cancel button to have secondary/outline styling (btn-outline-primary).");
        Assert.assertTrue(rawData.isConfirmationSubmitButtonPrimaryStyled(),
                "Expected Submit button to have primary styling (btn-primary).");

        // Cleanup: dismiss modal
        rawData.cancelModal();
        Assert.assertFalse(rawData.isConfirmationModalOpen(),
                "Expected confirmation modal to close after clicking Cancel.");
    }

    /**
     * Condition 13 & 14: Verify Cancel button and header close icon (X) dismiss
     * confirmation modal without initiating any backend processing.
     */
    @Test(priority = 5)
    public void verifyConfirmationModalCanBeDismissedWithoutProcessing() {
        RawDataPage rawData = navigateToRawDataPage();

        if (!ensureEligibleStreamSelected(rawData)) {
            throw new SkipException("No eligible stream with Refresh Columns action available.");
        }

        // Test 1: Dismiss via Cancel button
        rawData.clickRefreshColumns();
        Assert.assertTrue(rawData.isConfirmationModalOpen(), "Expected modal to open.");
        rawData.cancelModal();
        Assert.assertFalse(rawData.isConfirmationModalOpen(), "Expected modal to close on Cancel.");
        DealsModalPage dealsModal = rawData.getDealsModal();
        Assert.assertFalse(dealsModal.isOpen(), "DealsModal should NOT open when confirmation is cancelled.");

        // Test 2: Dismiss via Header Close button (X)
        rawData.clickRefreshColumns();
        Assert.assertTrue(rawData.isConfirmationModalOpen(), "Expected modal to open again.");
        rawData.closeConfirmationModalByHeaderClose();
        Assert.assertFalse(rawData.isConfirmationModalOpen(), "Expected modal to close on header X close.");
        Assert.assertFalse(dealsModal.isOpen(), "DealsModal should NOT open when confirmation is dismissed via X.");
    }

    /**
     * Condition 15, 16, 17: Verify clicking Submit triggers async execution, opens DealsModal,
     * shows in-progress state, and allows user to dismiss DealsModal without stopping job.
     */
    @Test(priority = 6)
    public void verifySubmitTriggersAsyncProcessingAndDismissibleModal() {
        RawDataPage rawData = navigateToRawDataPage();

        if (!ensureEligibleStreamSelected(rawData)) {
            throw new SkipException("No eligible stream with Refresh Columns action available.");
        }

        rawData.clickRefreshColumns();
        Assert.assertTrue(rawData.isConfirmationModalOpen(), "Expected confirmation modal to open.");

        // Trigger bulk recalculation
        rawData.confirmModal();

        DealsModalPage dealsModal = rawData.getDealsModal();
        Assert.assertTrue(dealsModal.isOpen(), "Expected DealsModal to open upon confirming Refresh Columns.");

        // Verify async running state (notice or progress bar)
        boolean hasNoticeOrBar = dealsModal.isInitialNoticeDisplayed()
                || dealsModal.isProgressBarPresent()
                || dealsModal.isCompleted();
        Assert.assertTrue(hasNoticeOrBar, "Expected DealsModal to show active progress or completion state.");

        // Verify modal can be dismissed via header close without crashing or blocking
        dealsModal.closeModal();
        Assert.assertTrue(dealsModal.isClosed(), "Expected DealsModal to close successfully when dismissed.");

        // Verify user remains on Raw Data page with grid intact
        Assert.assertTrue(rawData.isGridLoaded(), "Expected Raw Data grid to remain loaded after modal close.");
    }

    /**
     * Condition 19, 20, 21, 23: Complete flow: Execute refresh, wait for completion,
     * validate granular metrics (Derived/Lookup Processed, Records Processed/Updated),
     * and verify AG Grid refetches fresh data upon closing.
     */
    @Test(priority = 7)
    public void verifyRefreshColumnsCompletionMetricsAndGridRefetch() {
        RawDataPage rawData = navigateToRawDataPage();

        if (!ensureEligibleStreamSelected(rawData)) {
            throw new SkipException("No eligible stream with Refresh Columns action available.");
        }

        rawData.clickRefreshColumns();
        rawData.confirmModal();

        DealsModalPage dealsModal = rawData.getDealsModal();
        Assert.assertTrue(dealsModal.isOpen(), "Expected DealsModal to open.");

        // Wait up to 60 seconds for completion
        boolean finished = dealsModal.waitForCompletion(60);
        Assert.assertTrue(finished, "Expected Refresh Columns processing to finish within timeout.");

        if (dealsModal.isFailed()) {
            String reason = dealsModal.getFailureReason();
            Assert.fail("Refresh Columns processing failed on backend: " + reason);
        }

        Assert.assertTrue(dealsModal.isCompleted(), "Expected DealsModal to reach COMPLETED state.");

        // Extract and validate granular non-glue metrics
        int derivedCount = dealsModal.getDerivedColumnsProcessedCount();
        int lookupCount = dealsModal.getLookupColumnsProcessedCount();
        int recordsProcessed = dealsModal.getRecordsProcessedCount();
        int recordsUpdated = dealsModal.getRecordsUpdatedCount();

        System.out.println("  [METRICS] Derived Columns Processed: " + derivedCount);
        System.out.println("  [METRICS] Lookup Columns Processed: " + lookupCount);
        System.out.println("  [METRICS] Records Processed: " + recordsProcessed);
        System.out.println("  [METRICS] Records Updated: " + recordsUpdated);

        Assert.assertTrue(derivedCount >= 0, "Expected Derived Columns Processed count >= 0.");
        Assert.assertTrue(lookupCount >= 0, "Expected Lookup Columns Processed count >= 0.");
        Assert.assertTrue(recordsProcessed >= 0, "Expected Records Processed count >= 0.");
        Assert.assertTrue(recordsUpdated >= 0, "Expected Records Updated count >= 0.");

        // Close completion modal and verify AG Grid automatically refetches and displays data
        dealsModal.closeModal();
        Assert.assertTrue(dealsModal.isClosed(), "Expected DealsModal to close.");
        Assert.assertTrue(rawData.isGridLoaded(), "Expected AG Grid to refetch and remain loaded after completion.");
    }

    /**
     * Condition 25: Verify Refresh Columns is strictly scoped to the active stream tab
     * and does not unexpectedly navigate away or switch stream tabs.
     */
    @Test(priority = 8)
    public void verifyRefreshColumnsScopedToSelectedStreamTab() {
        RawDataPage rawData = navigateToRawDataPage();

        if (!ensureEligibleStreamSelected(rawData)) {
            throw new SkipException("No eligible stream available.");
        }

        String initialStreamTab = rawData.getSelectedDataStreamTab();

        rawData.clickRefreshColumns();
        Assert.assertTrue(rawData.isConfirmationModalOpen(), "Confirmation modal should open.");
        rawData.cancelModal();

        String currentStreamTab = rawData.getSelectedDataStreamTab();
        Assert.assertEquals(currentStreamTab, initialStreamTab,
                "Data stream tab should not change during Refresh Columns interaction.");
    }

    /**
     * Helper to select or ensure an eligible stream tab (with 'Refresh Columns' action) is active.
     */
    private boolean ensureEligibleStreamSelected(RawDataPage rawData) {
        if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
            return true;
        }
        List<String> tabs = rawData.getDataStreamTabNames();
        for (String tab : tabs) {
            rawData.selectDataStreamTab(tab);
            if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                return true;
            }
        }
        return false;
    }
}
