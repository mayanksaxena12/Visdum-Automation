package tests.resources;

import java.io.File;
import Base.DriverFactory;
import Base.ResourcesBaseTest;
import Pages.ResourceFormModal;
import Pages.ResourcesPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Validation tests covering Yup client-side schema and input constraints
 * on the Add Resource modal.
 */
public class ResourceValidationTest extends ResourcesBaseTest {

    @Test
    public void verifyRequiredFieldsOnBlankSubmit() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceFormModal modal = new ResourceFormModal(DriverFactory.getDriver());

        page.openAddResource();
        Assert.assertTrue(modal.isModalOpen(), "Add Resource modal should open.");

        modal.submit();

        Assert.assertTrue(modal.isValidationMessageVisible("Resource name is required."),
                "Expected required validation error for resource name.");
        Assert.assertTrue(modal.isValidationMessageVisible("Please assign the resource to at least one Role, Department, or User."),
                "Expected collective assignment error when roles, departments, and users are unselected.");
        Assert.assertTrue(modal.isValidationMessageVisible("Please upload a file (PDF/MP4)."),
                "Expected file upload required error.");

        modal.cancel();
    }

    @Test
    public void verifyNameMinimumLengthValidation() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceFormModal modal = new ResourceFormModal(DriverFactory.getDriver());

        page.openAddResource();
        modal.enterResourceName("Ab"); // < 3 characters
        modal.submit();

        Assert.assertTrue(modal.isValidationMessageVisible("Resource name must be at least 3 characters."),
                "Expected min length error when resource name has fewer than 3 characters.");

        modal.cancel();
    }

    @Test
    public void verifyNameMaximumLengthValidation() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceFormModal modal = new ResourceFormModal(DriverFactory.getDriver());

        page.openAddResource();
        modal.enterResourceName("A".repeat(105)); // > 100 characters
        modal.submit();

        Assert.assertTrue(modal.isValidationMessageVisible("Resource name cannot exceed 100 characters."),
                "Expected max length error when resource name exceeds 100 characters.");

        modal.cancel();
    }

    @Test
    public void verifyAssignmentValidationClearsOnRoleSelection() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceFormModal modal = new ResourceFormModal(DriverFactory.getDriver());

        page.openAddResource();
        modal.submit();
        Assert.assertTrue(modal.isValidationMessageVisible("Please assign the resource to at least one Role, Department, or User."));

        modal.selectRole("Manager");
        Assert.assertFalse(modal.isValidationMessageVisible("Please assign the resource to at least one Role, Department, or User."),
                "Assignment validation error should clear once a role is chosen.");

        modal.cancel();
    }

    @Test
    public void verifyFileRemovalRestoresRequiredError() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceFormModal modal = new ResourceFormModal(DriverFactory.getDriver());

        page.openAddResource();

        File samplePdf = new File("src/test/resources/sample-files/sample.pdf");
        if (samplePdf.exists()) {
            modal.uploadFile(samplePdf.getAbsolutePath());
            Assert.assertTrue(modal.isFileUploaded(), "Uploaded file card should appear.");

            modal.removeUploadedFile();
            modal.submit();

            Assert.assertTrue(modal.isValidationMessageVisible("Please upload a file (PDF/MP4)."),
                    "File required validation should appear after clearing the uploaded file.");
        }

        modal.cancel();
    }

    @Test
    public void verifyModalCancelDismisses() {
        ResourcesPage page = new ResourcesPage(DriverFactory.getDriver());
        ResourceFormModal modal = new ResourceFormModal(DriverFactory.getDriver());

        page.openAddResource();
        Assert.assertTrue(modal.isModalOpen(), "Modal should be open.");

        modal.cancel();
        Assert.assertTrue(modal.isModalClosed(), "Modal should close on cancel.");
    }
}
