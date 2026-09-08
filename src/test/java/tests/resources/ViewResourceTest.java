package tests.resources;

import Base.DriverFactory;
import Base.ResourcesBaseTest;
import Pages.ResourcePreviewModal;
import Pages.ResourcesPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the inline "View" button and Preview modal on the Resources grid.
 */
public class ViewResourceTest extends ResourcesBaseTest {

    @Test
    public void verifyViewResourcePreview() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourcePreviewModal preview = new ResourcePreviewModal(DriverFactory.getDriver());

        Assert.assertTrue(page.isGridLoaded(), "Resources grid should be loaded.");

        String resourceName = System.getProperty("search.resource", "");
        if (resourceName.isBlank()) {
            try {
                resourceName = page.getFirstRowValue("name");
            } catch (Exception ignored) {}
        }

        if (resourceName != null && !resourceName.isBlank()) {
            page.openView(resourceName);
            Assert.assertTrue(preview.isPreviewOpen(), "Preview modal should open for '" + resourceName + "'.");

            boolean hasViewer = preview.isDocumentViewerDisplayed() || preview.isVideoPlayerDisplayed();
            Assert.assertTrue(hasViewer, "Preview modal should contain an iframe document viewer or a video player.");

            preview.closePreview();
        }
    }
}
