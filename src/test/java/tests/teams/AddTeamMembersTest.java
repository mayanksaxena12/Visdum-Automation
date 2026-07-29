package tests.teams;

import Base.DriverFactory;
import Base.TeamsBaseTest;
import Pages.AddTeamMembersModal;
import Pages.TeamsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

/**
 * Covers the "Add Members" flow (row dropdown -> Add Members -> components/UserAssignModal.tsx),
 * which had a {@code TeamsPage.openAddMembers()} hook but no test exercising it end-to-end.
 */
public class AddTeamMembersTest extends TeamsBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireAddMembersPermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void addFirstAvailableUserToTeam() {
        String team = System.getProperty("test.team.existing", "");
        if (team.isBlank()) {
            throw new IllegalArgumentException(
                    "Set -Dtest.team.existing=<team name> for the add-members test.");
        }
        TeamsPage teams = new TeamsPage(DriverFactory.getDriver());
        AddTeamMembersModal modal = new AddTeamMembersModal(DriverFactory.getDriver());

        teams.search(team);
        teams.openAddMembers(team);

        Assert.assertTrue(modal.isOpen(), "Expected the Add Members modal to open.");
        modal.selectFirstAvailableUser();
        modal.confirmAdd();
    }
}
