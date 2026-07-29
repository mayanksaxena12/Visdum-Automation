package tests.teams;

import Base.DriverFactory;
import Base.TeamsBaseTest;
import Pages.TeamFormPage;
import Pages.TeamsPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

public class EditTeamTest extends TeamsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireEditPermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void editTeamName() {
        String team = System.getProperty("test.team.existing", "");
        if (team.isBlank()) {
            throw new IllegalArgumentException("Set -Dtest.team.existing=<team name> for the edit test.");
        }
        TeamsPage teams = new TeamsPage(DriverFactory.getDriver());
        TeamFormPage form = new TeamFormPage(DriverFactory.getDriver());

        teams.search(team);
        teams.openEditTeam(team);
        form.enterTeamName(System.getProperty("test.team.updated.name", "Updated Automation Team"));
        form.saveEditedTeam();
    }
}
