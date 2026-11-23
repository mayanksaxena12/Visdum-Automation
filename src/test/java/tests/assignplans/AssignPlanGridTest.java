package tests.assignplans;

import Base.AssignPlansBaseTest;
import Pages.AssignPlansPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AssignPlanGridTest extends AssignPlansBaseTest {

    @Test(description = "Verify searching for an assigned plan participant in AG-Grid")
    public void searchAssignedPlanParticipant() {
        AssignPlansPage assignPage = new AssignPlansPage(Base.DriverFactory.getDriver());

        String term = System.getProperty("test.assignplan.search", "Mayank");
        assignPage.searchAssignedPlan(term);
        Assert.assertTrue(assignPage.isRowListed(term),
                "Expected an assignment for '" + term + "' to be listed after searching (set -Dtest.assignplan.search).");
        assignPage.clearSearch();
    }
}
