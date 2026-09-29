package tests.plans;

import Base.PlansBaseTest;
import Pages.CreatePlanWizardPage;
import Pages.PlansPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreatePlanValidationTest extends PlansBaseTest {

    @Test(description = "Attempting to proceed with empty Plan Name triggers Yup validation error")
    public void emptyPlanNameShowsRequiredError() {
        PlansPage plansPage = new PlansPage(org.openqa.selenium.support.ui.WebDriverWait.class.cast(null) == null 
                ? Base.DriverFactory.getDriver() : Base.DriverFactory.getDriver());
        plansPage.clickCreatePlanAdvance();

        CreatePlanWizardPage wizard = new CreatePlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Create Plan wizard should be loaded");

        // Click next without filling required fields
        wizard.clickNext();

        Assert.assertTrue(wizard.hasValidationError("Plan Name is required."),
                "Validation message 'Plan Name is required.' should be displayed");
    }

    @Test(description = "Plan name longer than 70 characters triggers max length error")
    public void planNameExceeding70CharsShowsError() {
        PlansPage plansPage = new PlansPage(Base.DriverFactory.getDriver());
        plansPage.clickCreatePlanAdvance();

        CreatePlanWizardPage wizard = new CreatePlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Create Plan wizard should be loaded");

        String longName = "A".repeat(75);
        wizard.enterPlanName(longName);
        wizard.clickNext();

        Assert.assertTrue(wizard.hasValidationError("Plan Name cannot exceed 70 characters."),
                "Validation message 'Plan Name cannot exceed 70 characters.' should be displayed");
    }

    @Test(description = "Missing Payout Period dropdown triggers required validation error")
    public void missingPayoutPeriodShowsRequiredError() {
        PlansPage plansPage = new PlansPage(Base.DriverFactory.getDriver());
        plansPage.clickCreatePlanAdvance();

        CreatePlanWizardPage wizard = new CreatePlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Create Plan wizard should be loaded");

        wizard.enterPlanName("Valid Plan Name");
        wizard.clickNext();

        Assert.assertTrue(wizard.hasValidationError("Payout Period is required."),
                "Validation message 'Payout Period is required.' should be displayed");
    }

    @Test(description = "Clicking Cancel from Step 1 navigates back to Plans list")
    public void cancelButtonReturnsToPlansList() {
        PlansPage plansPage = new PlansPage(Base.DriverFactory.getDriver());
        plansPage.clickCreatePlanAdvance();

        CreatePlanWizardPage wizard = new CreatePlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Create Plan wizard should be loaded");

        wizard.clickCancel();
        Assert.assertTrue(Base.DriverFactory.getDriver().getCurrentUrl().contains("/plans"),
                "Should navigate back to /plans URL after cancelling");
    }
}
