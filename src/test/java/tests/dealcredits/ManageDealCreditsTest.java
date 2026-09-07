package tests.dealcredits;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.DealCreditsPage;
import Pages.ManageDealCreditsDrawerPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the Manage Deal Credits drawer (ManageDealCreditsDrawer.tsx).
 */
public class ManageDealCreditsTest extends BaseTest {

    @Test
    public void verifyManageDealCreditsDrawerOpenAndClose() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DealCreditsPage dealCredits = new DealCreditsPage(DriverFactory.getDriver());
        ManageDealCreditsDrawerPage manageDrawer = new ManageDealCreditsDrawerPage(DriverFactory.getDriver());

        dashboard.navigateToDealCredits();
        Assert.assertTrue(dealCredits.isLoaded(), "Expected Deal Credits page to load.");

        dealCredits.clickManageDealCredits();
        Assert.assertTrue(manageDrawer.isOpen(), "Expected Manage Deal Credits drawer to open.");

        // Exercise search within drawer
        manageDrawer.searchDataStreams("Deals");

        // Close drawer
        manageDrawer.close();
        Assert.assertFalse(manageDrawer.isOpen(), "Expected Manage Deal Credits drawer to close.");
    }
}
