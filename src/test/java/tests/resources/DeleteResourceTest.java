package tests.resources;

import Base.DriverFactory;
import Base.ResourcesBaseTest;
import Pages.ResourceDeleteModal;
import Pages.ResourcesPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/**
 * Tests for deleting resources (cancel flow and confirmed deletion flow).
 */
public class DeleteResourceTest extends ResourcesBaseTest {

    @Test
    public void verifyCancelDeleteLeavesResourceIntact() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceDeleteModal deleteModal = new ResourceDeleteModal(DriverFactory.getDriver());

        String targetResource = System.getProperty("delete.resource", "");
        if (targetResource.isBlank()) {
            try {
                targetResource = page.getFirstRowValue("name");
            } catch (Exception ignored) {}
        }

        if (targetResource != null && !targetResource.isBlank()) {
            page.openDeleteResource(targetResource);
            Assert.assertTrue(deleteModal.isDeleteModalOpen(), "Delete confirmation modal should open.");

            deleteModal.cancelDelete();
            Assert.assertFalse(deleteModal.isDeleteModalOpen(), "Delete modal should close on cancel.");

            Assert.assertTrue(page.isRowListed(targetResource),
                    "Resource '" + targetResource + "' should still be listed in the grid after cancel.");
        }
    }

    @Test
    public void verifyConfirmDeleteRemovesResource() {
        ExecutionGuard.requireDestructiveTestsEnabled();

        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceDeleteModal deleteModal = new ResourceDeleteModal(DriverFactory.getDriver());

        String targetResource = System.getProperty("delete.resource", "");
        if (targetResource.isBlank()) {
            try {
                targetResource = page.getFirstRowValue("name");
            } catch (Exception ignored) {}
        }

        if (targetResource != null && !targetResource.isBlank()) {
            page.openDeleteResource(targetResource);
            deleteModal.confirmDelete();

            Assert.assertTrue(deleteModal.isDeleteSuccessToastDisplayed(),
                    "Expected 'Resource Deleted Successfully' toast notification.");

            page.search(targetResource);
            Assert.assertFalse(page.isRowListed(targetResource),
                    "Resource '" + targetResource + "' should no longer be listed after deletion.");
        }
    }
}
