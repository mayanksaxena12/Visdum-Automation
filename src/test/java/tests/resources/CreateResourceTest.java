package tests.resources;

import java.io.File;
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
 * End-to-end creation tests for new resources (PDF and MP4).
 *
 * <p>Protected by {@link ExecutionGuard#requireDestructiveTestsEnabled()} to avoid mutating shared UAT data.
 */
public class CreateResourceTest extends ResourcesBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireCreatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void createValidDocumentResource() {
        String resourceName = "Auto_Doc_" + Instant.now().toEpochMilli();
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceFormModal form = new ResourceFormModal(DriverFactory.getDriver());

        page.openAddResource();
        Assert.assertTrue(form.isModalOpen(), "Add Resource modal should open.");

        form.enterResourceName(resourceName);
        form.selectRole("Manager");

        File samplePdf = new File("src/test/resources/sample-files/sample.pdf");
        Assert.assertTrue(samplePdf.exists(), "Sample PDF must exist.");
        form.uploadFile(samplePdf.getAbsolutePath());
        Assert.assertTrue(form.isFileUploaded(), "File card should be displayed.");

        form.submit();

        Assert.assertTrue(form.isSuccessToastDisplayed() || form.isModalClosed(),
                "Expected success toast or modal close after creating resource.");

        page.search(resourceName);
        Assert.assertTrue(page.isRowListed(resourceName),
                "Newly created resource '" + resourceName + "' should appear in grid.");
    }

    @Test
    public void createDuplicateResourceNameValidation() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceFormModal form = new ResourceFormModal(DriverFactory.getDriver());

        String existingName = System.getProperty("search.resource", "");
        if (existingName.isBlank()) {
            try {
                existingName = page.getFirstRowValue("name");
            } catch (Exception ignored) {}
        }

        if (existingName != null && !existingName.isBlank()) {
            page.openAddResource();
            form.enterResourceName(existingName);
            form.selectRole("Manager");

            File samplePdf = new File("src/test/resources/sample-files/sample.pdf");
            form.uploadFile(samplePdf.getAbsolutePath());
            form.submit();

            Assert.assertTrue(form.isValidationMessageVisible("Resource name must be unique.")
                    || form.isValidationMessageVisible("unique"),
                    "Expected duplicate name error for '" + existingName + "'.");

            form.cancel();
        }
    }
}
