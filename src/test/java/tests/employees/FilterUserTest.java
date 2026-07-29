package tests.employees;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.UserFilterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Covers the advanced "Custom Filter" side drawer (funnel icon in UsersList.tsx, rendered by
 * components/CustomeFilter/CustomFilter.tsx).
 *
 * <p>Requires the logged-in UAT user to have the {@code USER_STREAM} permission -- the funnel icon
 * only renders inside that permission check, so this test will fail fast with a clear timeout if
 * the configured account lacks it. Operator and Value options are populated from the backend at
 * runtime, so this only pins the Column choice and accepts the first Operator/Value offered.
 */
public class FilterUserTest extends BaseTest {

    @Test
    public void applyingRoleFilterClosesThePanel() {
        UserFilterPage filter = new UserFilterPage(DriverFactory.getDriver());

        filter.openFilterPanel();
        Assert.assertTrue(filter.isPanelOpen());

        filter.selectColumn("Role");
        filter.selectFirstOperator();
        filter.setValueBestEffort("Individual Contributor");
        filter.applyFilters();

        Assert.assertTrue(
                DriverFactory.getDriver().findElements(org.openqa.selenium.By.xpath(
                        "//button[normalize-space()='Apply Filters']")).isEmpty(),
                "Expected the filter drawer to close after applying filters.");
    }
}
