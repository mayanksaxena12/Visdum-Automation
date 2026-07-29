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
 * Activates an existing, currently-Inactive team via the row dropdown's status toggle
 * (TeamsList.tsx shows "Are you sure you want to activate?" before confirming). Requires the
 * target team to already be Inactive -- see {@link DeactivateTeamTest} for the reverse transition.
 */
public class ActivateTeamTest extends TeamsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireActivatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void activateInactiveTeam() {
        String team = System.getProperty("test.team.existing", "");
        if (team.isBlank()) {
            throw new IllegalArgumentException(
                    "Set -Dtest.team.existing=<team name> for the activate test.");
        }
        TeamsPage teams = new TeamsPage(DriverFactory.getDriver());
        TeamStatusModal modal = new TeamStatusModal(DriverFactory.getDriver());

        teams.search(team);
        Assert.assertEquals(teams.statusOf(team), "Inactive",
                "This test activates a team, so it must start Inactive. "
                        + "Use a different -Dtest.team.existing or run DeactivateTeamTest first.");

        teams.openToggleStatus(team);
        Assert.assertTrue(modal.isConfirmationVisible("Are you sure you want to activate?"));
        modal.confirm();

        teams.search(team);
        Assert.assertEquals(teams.statusOf(team), "Active");
    }
}
