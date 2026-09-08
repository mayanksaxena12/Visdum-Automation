package tests.rawdata;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.RawDataPage;
import Pages.RawDataViewPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for Raw Data actions: header actions modal cancellation and row view details drawer.
 */
public class RawDataActionsTest extends BaseTest {

    @Test
    public void verifyFetchNewDataModalCanBeCancelled() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        if (rawData.isFetchNewDataPresent()) {
            rawData.clickFetchNewData();
            Assert.assertTrue(rawData.isConfirmationModalOpen(),
                    "Expected confirmation modal to open for Fetch New Data.");

            String modalText = rawData.getConfirmationModalText();
            Assert.assertTrue(modalText.toLowerCase().contains("fetch")
                            || modalText.toLowerCase().contains("sure"),
                    "Expected modal prompt to ask for confirmation.");

            rawData.cancelModal();
            Assert.assertFalse(rawData.isConfirmationModalOpen(),
                    "Expected confirmation modal to close on Cancel.");
        }
    }

    @Test
    public void verifyViewDetailsDrawer() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());
        RawDataViewPage viewPage = new RawDataViewPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        String recordIdentifier = System.getProperty("test.rawdata.record", "");
        if (recordIdentifier.isBlank()) {
            try {
                recordIdentifier = rawData.getFirstRowValue("opportunity_name");
            } catch (Exception ignored) {
            }
        }
        if (recordIdentifier.isBlank()) {
            try {
                recordIdentifier = rawData.getFirstRowValue("name");
            } catch (Exception ignored) {
            }
        }

        if (!recordIdentifier.isBlank() && rawData.isRowListed(recordIdentifier)) {
            rawData.openViewDetails(recordIdentifier);
            Assert.assertTrue(viewPage.isOpen(), "Expected Raw Data Details drawer to open.");

            viewPage.close();
        }
    }

    @Test
    public void verifyRefreshColumnsModalCanBeCancelled() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        if (rawData.isHeaderActionsPresent()) {
            rawData.openHeaderActionsMenu();
            if (rawData.isHeaderActionPresent("Refresh Columns")) {
                rawData.clickHeaderAction("Refresh Columns");
                Assert.assertTrue(rawData.isConfirmationModalOpen(),
                        "Expected confirmation modal to open for Refresh Columns.");

                String modalText = rawData.getConfirmationModalText();
                Assert.assertTrue(modalText.toLowerCase().contains("refresh")
                                || modalText.toLowerCase().contains("derived")
                                || modalText.toLowerCase().contains("lookup")
                                || modalText.toLowerCase().contains("sure"),
                        "Expected modal prompt to ask for column refresh confirmation.");

                rawData.cancelModal();
                Assert.assertFalse(rawData.isConfirmationModalOpen(),
                        "Expected confirmation modal to close on Cancel.");
            }
        }
    }
}
