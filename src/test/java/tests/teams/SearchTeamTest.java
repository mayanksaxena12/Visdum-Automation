package tests.teams;

import Base.DriverFactory;
import Base.TeamsBaseTest;
import Pages.TeamsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SearchTeamTest extends TeamsBaseTest {

    @Test
    public void verifySearchTeam() {

        TeamsPage page = new TeamsPage(DriverFactory.getDriver());

        page.search("Sales");

        // Assert.assertTrue(
        //         DriverFactory.getDriver()
        //                 .getPageSource()
        //                 .contains("Sales"));

        Assert.assertTrue(page.isRowListed("Sales"),
                "Expected a team row to match 'Sales'.");
    }
}
