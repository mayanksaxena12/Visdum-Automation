package tests.dealcredits;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.DealCreditsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for searching records within Deal Credits (DealCreditsWrapper.tsx / DealCreditsTable.tsx).
 */
public class SearchDealCreditsTest extends BaseTest {

    @Test
    public void verifyDealCreditsPageLoadAndSearch() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DealCreditsPage dealCredits = new DealCreditsPage(DriverFactory.getDriver());

        dashboard.navigateToDealCredits();
        Assert.assertTrue(dealCredits.isLoaded(), "Expected Deal Credits page to load.");

        String keyword = System.getProperty("test.dealcredits.search", "Deal");
        dealCredits.search(keyword);
        Assert.assertTrue(dealCredits.isGridLoaded(), "Expected grid to remain loaded after search.");

        dealCredits.clearSearch();
    }

    @Test
    public void verifySearchWithNoResults() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DealCreditsPage dealCredits = new DealCreditsPage(DriverFactory.getDriver());

        dashboard.navigateToDealCredits();
        Assert.assertTrue(dealCredits.isLoaded(), "Expected Deal Credits page to load.");

        dealCredits.search("non_existent_credit_xyz_99999");
        Assert.assertFalse(dealCredits.isRowListed("non_existent_credit_xyz_99999"),
                "Expected non-existent credit to not be found in grid.");

        dealCredits.clearSearch();
    }
}
