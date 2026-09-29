package tests.plans;

import Base.PlansBaseTest;
import Pages.CreatePlanWizardPage;
import Pages.ExpressionBuilderHelper;
import Pages.PlansPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExpressionBuilderTest extends PlansBaseTest {

    @Test(description = "Expression Builder flags unbalanced parentheses syntax error")
    public void verifySyntaxErrorOnUnbalancedParentheses() {
        PlansPage plansPage = new PlansPage(Base.DriverFactory.getDriver());
        plansPage.clickCreatePlanAdvance();

        CreatePlanWizardPage wizard = new CreatePlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Wizard should load");

        // Fill step 1 to proceed to rule step
        String uniquePlan = "Plan_Formula_" + System.currentTimeMillis();
        wizard.enterPlanName(uniquePlan);
        wizard.selectPayoutPeriod("Monthly");
        wizard.clickNext();

        // On rule creation step, use Expression Builder
        ExpressionBuilderHelper builder = wizard.expressionBuilder();
        builder.focusBuilder();
        builder.insertOpeningBracket();
        builder.typeToken("100");
        builder.insertOperator("+");
        builder.typeToken("50");
        // Intentionally omit closing bracket

        // Assert syntax error is surfaced
        Assert.assertTrue(builder.hasSyntaxError(), "Expression Builder should flag unmatched bracket error");
    }

    @Test(description = "Expression Builder flags leading operator syntax error")
    public void verifySyntaxErrorOnLeadingOperator() {
        PlansPage plansPage = new PlansPage(Base.DriverFactory.getDriver());
        plansPage.clickCreatePlanAdvance();

        CreatePlanWizardPage wizard = new CreatePlanWizardPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(wizard.isLoaded(), "Wizard should load");

        wizard.enterPlanName("Plan_Operator_" + System.currentTimeMillis());
        wizard.selectPayoutPeriod("Monthly");
        wizard.clickNext();

        ExpressionBuilderHelper builder = wizard.expressionBuilder();
        builder.focusBuilder();
        builder.insertOperator("*");
        builder.typeToken("20");

        Assert.assertTrue(builder.hasSyntaxError(), "Expression Builder should reject starting with an operator");
    }
}
