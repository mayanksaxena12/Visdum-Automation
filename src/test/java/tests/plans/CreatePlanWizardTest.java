package tests.plans;

import Base.PlansBaseTest;
import Pages.CreatePlanWizardPage;
import Pages.PlansPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreatePlanWizardTest extends PlansBaseTest {

    @Test(description = "Completing Step 1 with valid data moves to Step 2 in Advance Plan Wizard")
    public void completeStep1AndProceed() {
        PlansPage plansPage = new PlansPage(Base.DriverFactory.getDriver());
        plansPage.clickCreatePlanAdvance();

        CreatePlanWizardPage wizard = new CreatePlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Create Plan wizard should be loaded");

        String planName = "Auto_Plan_" + System.currentTimeMillis();
        wizard.enterPlanName(planName);
        wizard.selectPayoutPeriod("Monthly");
        wizard.enterDescription("Automated regression test plan description");

        wizard.clickNext();

        // Verify no Step 1 validation errors remain
        Assert.assertEquals(wizard.getValidationErrors().size(), 0,
                "Step 1 should complete with zero validation errors");
    }

    @Test(description = "Clicking 'Save to draft' from Step 1 persists draft and exits to plans list")
    public void savePlanAsDraftWorkflow() {
        PlansPage plansPage = new PlansPage(Base.DriverFactory.getDriver());
        plansPage.clickCreatePlanAdvance();

        CreatePlanWizardPage wizard = new CreatePlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Create Plan wizard should be loaded");

        String draftPlanName = "Draft_Plan_" + System.currentTimeMillis();
        wizard.enterPlanName(draftPlanName);
        wizard.selectPayoutPeriod("Quarterly");

        wizard.clickSaveToDraft();

        Assert.assertTrue(Base.DriverFactory.getDriver().getCurrentUrl().contains("/plans"),
                "Should redirect back to Plans list after saving draft");
    }
}
