package tests.teams;

import Base.DriverFactory;
import Base.TeamsBaseTest;
import Pages.TeamStatusModal;
import Pages.TeamsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/**
 * Deactivates an existing, currently-Active team via the row dropdown's status toggle
 * (TeamsList.tsx shows "Are you sure you want to deactivate?" before confirming). Requires the
 * target team to already be Active -- see {@link ActivateTeamTest} for the reverse transition.
 */
public class DeactivateTeamTest extends TeamsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireDeactivatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void deactivateActiveTeam() {
        String team = System.getProperty("test.team.existing", "");
        if (team.isBlank()) {
            throw new IllegalArgumentException(
                    "Set -Dtest.team.existing=<team name> for the deactivate test.");
        }
        TeamsPage teams = new TeamsPage(DriverFactory.getDriver());
        TeamStatusModal modal = new TeamStatusModal(DriverFactory.getDriver());

        teams.search(team);
        Assert.assertEquals(teams.statusOf(team), "Active",
                "This test deactivates a team, so it must start Active. "
                        + "Use a different -Dtest.team.existing or run ActivateTeamTest first.");

        teams.openToggleStatus(team);
        Assert.assertTrue(modal.isConfirmationVisible("Are you sure you want to deactivate?"));
        modal.confirm();

        teams.search(team);
        Assert.assertEquals(teams.statusOf(team), "Inactive");
    }
}
