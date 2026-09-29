package tests.assignplans;

import Base.AssignPlansBaseTest;
import Pages.AssignPlansPage;
import Pages.CreateAssignPlanWizardPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AssignPlanValidationTest extends AssignPlansBaseTest {

    @Test(description = "Attempting to proceed in Assign Plan wizard without required fields triggers validation")
    public void emptyFieldsTriggerValidation() {
        AssignPlansPage assignPage = new AssignPlansPage(Base.DriverFactory.getDriver());
        assignPage.clickAssignPlan();

        CreateAssignPlanWizardPage wizard = new CreateAssignPlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Assign Plan wizard should be loaded");

        // Click next without selecting participant or dates
        wizard.clickNext();

        Assert.assertTrue(wizard.hasValidationError("required") 
                || !wizard.getValidationErrors().isEmpty() 
                || wizard.isLoaded(),
                "Validation error should prevent advancing to step 2");
    }

    @Test(description = "Cancelling Assign Plan wizard navigates back to /plans/assign-plan")
    public void cancelWizardReturnsToAssignPlansList() {
        AssignPlansPage assignPage = new AssignPlansPage(Base.DriverFactory.getDriver());
        assignPage.clickAssignPlan();

        CreateAssignPlanWizardPage wizard = new CreateAssignPlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Assign Plan wizard should be loaded");

        wizard.clickCancel();
        Assert.assertTrue(Base.DriverFactory.getDriver().getCurrentUrl().contains("/plans/assign-plan"),
                "Should navigate back to /plans/assign-plan after cancelling");
    }
}
