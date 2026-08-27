package tests.teams;

import java.time.Instant;

import Base.DriverFactory;
import Base.TeamsBaseTest;
import Pages.TeamFormPage;
import Pages.TeamsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

public class CreateTeamTest extends TeamsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireCreatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void createValidTeam() {
        String id = String.valueOf(Instant.now().toEpochMilli());
        String teamName = "Automation Team " + id;

        TeamsPage teams = new TeamsPage(DriverFactory.getDriver());
        TeamFormPage form = new TeamFormPage(DriverFactory.getDriver());

        teams.openCreateTeam();
        form.enterTeamName(teamName);
        form.enterTeamDescription("Created by Selenium automation");
        form.submitNewTeam();

        teams.search(teamName);
        // Assert.assertTrue(teams.isTeamListed(teamName));
        Assert.assertTrue(teams.isRowListed(teamName));
    }
}
