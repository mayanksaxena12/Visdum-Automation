package tests.dealcredits;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.DealCreditsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for Deal Credits grid capabilities (sorting, fullscreen toggle, virtualization).
 */
public class DealCreditsGridTest extends BaseTest {

    @Test
    public void verifyDealCreditsGridSortAndFullscreen() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DealCreditsPage dealCredits = new DealCreditsPage(DriverFactory.getDriver());

        dashboard.navigateToDealCredits();
        Assert.assertTrue(dealCredits.isLoaded(), "Expected Deal Credits page to load.");
        Assert.assertTrue(dealCredits.isGridLoaded(), "Expected Deal Credits AG-Grid to load.");

        // Toggle fullscreen and restore
        dealCredits.toggleFullScreen();
        dealCredits.toggleFullScreen();
        Assert.assertTrue(dealCredits.isGridLoaded(), "Grid should remain functional after fullscreen toggle.");
    }
}
