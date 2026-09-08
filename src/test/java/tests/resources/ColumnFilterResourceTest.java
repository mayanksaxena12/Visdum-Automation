package tests.resources;

import Base.DriverFactory;
import Base.ResourcesBaseTest;
import Pages.ResourcesPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for AG-Grid column menu (Set Filter) on the Resources grid.
 */
public class ColumnFilterResourceTest extends ResourcesBaseTest {

    @Test
    public void verifyOpenColumnMenuOnName() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        page.openColumnMenu("name");
        Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for 'name'.");
        page.closeColumnMenu();
    }

    @Test
    public void verifyColumnFilterOnResourceType() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        page.openColumnMenu("type");
        Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for 'type'.");
        page.toggleFirstColumnFilterValue();
        page.closeColumnMenu();
    }
}
