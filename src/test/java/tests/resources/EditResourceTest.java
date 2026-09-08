package tests.resources;

import java.time.Instant;
import Base.DriverFactory;
import Base.ResourcesBaseTest;
import Pages.ResourceFormModal;
import Pages.ResourcesPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/**
 * Tests for editing/updating an existing resource.
 *
 * <p>Protected by {@link ExecutionGuard#requireDestructiveTestsEnabled()}.
 */
public class EditResourceTest extends ResourcesBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireEditPermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void editResourceName() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceFormModal form = new ResourceFormModal(DriverFactory.getDriver());

        String targetResource = System.getProperty("edit.resource", "");
        if (targetResource.isBlank()) {
            try {
                targetResource = page.getFirstRowValue("name");
            } catch (Exception ignored) {}
        }

        if (targetResource != null && !targetResource.isBlank()) {
            page.openEditResource(targetResource);
            Assert.assertTrue(form.isModalOpen(), "Update Resource modal should open.");
            Assert.assertEquals(form.getModalTitle(), "Update Resource");

            String updatedName = targetResource + "_edited_" + Instant.now().toEpochMilli();
            form.clearResourceName();
            form.enterResourceName(updatedName);
            form.submit();

            Assert.assertTrue(form.isSuccessToastDisplayed() || form.isModalClosed(),
                    "Expected update success toast or modal close.");

            page.search(updatedName);
            Assert.assertTrue(page.isRowListed(updatedName),
                    "Updated resource name '" + updatedName + "' should appear in grid.");
        }
    }
}
