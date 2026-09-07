package tests.rawdata;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.RawDataPage;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for switching between Data Stream tabs in Raw Data (RawDataListHeader.tsx).
 */
public class RawDataStreamTabsTest extends BaseTest {

    @Test
    public void verifyDataStreamTabsDisplayed() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        List<String> tabs = rawData.getDataStreamTabNames();
        Assert.assertFalse(tabs.isEmpty(), "Expected at least one data stream tab to be displayed.");

        String activeTab = rawData.getSelectedDataStreamTab();
        Assert.assertFalse(activeTab.isBlank(), "Expected an active data stream tab.");
        Assert.assertTrue(tabs.contains(activeTab), "Active tab should be among the displayed tabs.");
    }

    @Test
    public void verifySwitchBetweenDataStreamTabs() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        RawDataPage rawData = new RawDataPage(DriverFactory.getDriver());

        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Expected Raw Data page to load.");

        List<String> tabs = rawData.getDataStreamTabNames();
        if (tabs.size() > 1) {
            String initialTab = rawData.getSelectedDataStreamTab();
            String targetTab = tabs.stream()
                    .filter(t -> !t.equalsIgnoreCase(initialTab))
                    .findFirst()
                    .orElse(tabs.get(1));

            // Switch to target tab
            rawData.selectDataStreamTab(targetTab);
            Assert.assertEquals(rawData.getSelectedDataStreamTab(), targetTab,
                    "Selected tab should update to target tab.");
            Assert.assertTrue(rawData.isGridLoaded(), "Grid should be loaded for newly selected tab.");

            // Switch back to initial tab
            rawData.selectDataStreamTab(initialTab);
            Assert.assertEquals(rawData.getSelectedDataStreamTab(), initialTab,
                    "Selected tab should switch back to initial tab.");
        }
    }
}
