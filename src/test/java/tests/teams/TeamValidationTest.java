package tests.teams;

import Base.DriverFactory;
import Base.TeamsBaseTest;
import Pages.TeamFormPage;
import Pages.TeamsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TeamValidationTest extends TeamsBaseTest {

    @Test
    public void requiredFieldsAreValidatedOnSubmit() {
        TeamsPage teams = new TeamsPage(DriverFactory.getDriver());
        TeamFormPage form = new TeamFormPage(DriverFactory.getDriver());

        teams.openCreateTeam();
        form.submitNewTeam();

        Assert.assertTrue(form.isValidationMessageVisible("Team Name is required"));
    }
}
