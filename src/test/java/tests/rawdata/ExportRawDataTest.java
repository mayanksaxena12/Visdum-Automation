package tests.rawdata;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.RawDataPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for exporting raw data and template files (RawDataListHeader.tsx).
 */
public class ExportRawDataTest extends BaseTest {

    @Test
    public void verifyExportDropdownMenu() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        rawData.openExportDropdown();
        Assert.assertTrue(rawData.isExportDropdownOpen(), "Expected Export dropdown menu to open.");
    }

    @Test
    public void verifyExportDataDownload() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        rawData.openExportDropdown();
        rawData.clickExportData();

        Assert.assertTrue(rawData.isLoaded(), "Page should remain stable after triggering Export Data.");
    }

    @Test
    public void verifyExportTemplateDownload() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        rawData.openExportDropdown();
        rawData.clickExportTemplate();

        Assert.assertTrue(rawData.isLoaded(), "Page should remain stable after triggering Export Template.");
    }
}
