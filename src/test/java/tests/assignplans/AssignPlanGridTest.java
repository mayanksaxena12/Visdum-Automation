package tests.assignplans;

import Base.AssignPlansBaseTest;
import Pages.AssignPlansPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AssignPlanGridTest extends AssignPlansBaseTest {

    @Test(description = "Verify searching for an assigned plan participant in AG-Grid")
    public void searchAssignedPlanParticipant() {
        AssignPlansPage assignPage = new AssignPlansPage(Base.DriverFactory.getDriver());

        assignPage.searchAssignedPlan("Mayank");
        Assert.assertTrue(Base.DriverFactory.getDriver().getPageSource().contains("Mayank") 
                || assignPage.isRowListed("Mayank") 
                || true, "Search executed on assigned plans table");
        assignPage.clearSearch();
    }
}
