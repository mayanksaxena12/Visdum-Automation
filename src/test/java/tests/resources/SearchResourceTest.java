package tests.resources;

import java.time.Instant;
import Base.DriverFactory;
import Base.ResourcesBaseTest;
import Pages.ResourcesPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the quick search functionality on the Resources grid.
 */
public class SearchResourceTest extends ResourcesBaseTest {

    @Test
    public void verifySearchByExistingResource() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        String term = System.getProperty("search.resource", "");
        if (term.isBlank()) {
            try {
                term = page.getFirstRowValue("name");
            } catch (Exception ignored) {}
        }
        if (term.isBlank()) {
            term = "Test";
        }

        page.search(term);
        Assert.assertTrue(page.isRowListed(term),
                "Expected resource row matching '" + term + "' to be listed.");
    }

    @Test
    public void verifySearchWithNonExistentResource() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        String nonExistent = "NonExistent_" + Instant.now().toEpochMilli();
        page.search(nonExistent);

        Assert.assertFalse(page.isRowListed(nonExistent),
                "Non-existent resource should not match any rows.");
    }

    @Test
    public void verifyClearSearchRestoresGrid() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        int initialCount = page.getRowCount();

        String nonExistent = "DummyFilter_" + Instant.now().toEpochMilli();
        page.search(nonExistent);

        page.clearSearch();
        Assert.assertEquals(page.getSearchValue(), "", "Search input should be cleared.");
        if (initialCount > 0) {
            Assert.assertTrue(page.getRowCount() > 0, "Grid rows should be restored after clearing search.");
        }
    }
}
