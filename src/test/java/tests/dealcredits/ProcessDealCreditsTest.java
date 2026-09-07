package tests.dealcredits;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.DealCreditsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the Process Deal Credits modal dialog (ProcessDealCreditsModal.tsx).
 */
public class ProcessDealCreditsTest extends BaseTest {

    @Test
    public void verifyProcessDealCreditsModalCanBeCancelled() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DealCreditsPage dealCredits = new DealCreditsPage(DriverFactory.getDriver());

        dashboard.navigateToDealCredits();
        Assert.assertTrue(dealCredits.isLoaded(), "Expected Deal Credits page to load.");

        dealCredits.clickProcessDealCredits();
        Assert.assertTrue(dealCredits.isProcessModalOpen(),
                "Expected Process Deal Credits modal to open.");

        dealCredits.cancelProcessModal();
        Assert.assertFalse(dealCredits.isProcessModalOpen(),
                "Expected Process Deal Credits modal to close on Cancel.");
    }
}
