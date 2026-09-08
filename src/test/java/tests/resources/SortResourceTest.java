package tests.resources;

import Base.DriverFactory;
import Base.ResourcesBaseTest;
import Pages.ResourcesPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for AG-Grid column sorting on the Resources grid.
 */
public class SortResourceTest extends ResourcesBaseTest {

    @Test
    public void verifySortByName() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        page.sortByColumn("name");
        Assert.assertEquals(page.sortDirectionOf("name"), "ascending",
                "Name column should sort ascending on first header click.");

        page.sortByColumn("name");
        Assert.assertEquals(page.sortDirectionOf("name"), "descending",
                "Name column should sort descending on second header click.");
    }

    @Test
    public void verifySortByRole() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        page.sortByColumn("roles");
        Assert.assertEquals(page.sortDirectionOf("roles"), "ascending",
                "Roles column should sort ascending.");
    }

    @Test
    public void verifySortByDepartment() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        page.sortByColumn("department_names");
        Assert.assertEquals(page.sortDirectionOf("department_names"), "ascending",
                "Department column should sort ascending.");
    }

    @Test
    public void verifySortByResourceType() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        page.sortByColumn("type");
        Assert.assertEquals(page.sortDirectionOf("type"), "ascending",
                "Resource Type column should sort ascending.");
    }
}
