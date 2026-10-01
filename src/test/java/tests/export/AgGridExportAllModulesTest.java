package tests.export;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.UsersPage;
import Pages.TeamsPage;
import Pages.RawDataPage;
import Pages.DealCreditsPage;
import Pages.RateTablePage;
import Pages.PlansPage;
import Pages.AssignPlansPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ScreenRecorderUtil;

/**
 * End-to-end Automated AG-Grid Context Menu Export Test Suite.
 *
 * Covers right-click context menu export ("Export" -> "Excel Export")
 * across all major Visdum modules:
 *  - Users / Employees (/users/employees)
 *  - Teams (/users/teams)
 *  - Departments (/users/department)
 *  - Raw Data (/data/raw-data)
 *  - Deal Credits (/data/deal-credits)
 *  - Rate Tables (/plans/rate-table)
 *  - Plans (/plans/create-plan)
 *  - Assign Plans (/plans/assign-plan)
 */
public class AgGridExportAllModulesTest extends BaseTest {

    @Test(priority = 1, description = "Right-click AG-Grid and export Users data to Excel")
    public void exportUsersGrid() {
        WebDriver driver = DriverFactory.getDriver();
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.navigateToEmployees();

        UsersPage usersPage = new UsersPage(driver);
        Assert.assertTrue(usersPage.isGridLoaded(), "Users AG-Grid should be loaded.");

        ScreenRecorderUtil.captureFrame(driver, "Navigated to Users Grid");
        boolean exported = usersPage.rightClickAndExport();
        Assert.assertTrue(exported, "Users AG-Grid right-click Excel Export should trigger successfully.");
    }

    @Test(priority = 2, description = "Right-click AG-Grid and export Teams data to Excel")
    public void exportTeamsGrid() {
        WebDriver driver = DriverFactory.getDriver();
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.navigateToTeams();

        TeamsPage teamsPage = new TeamsPage(driver);
        Assert.assertTrue(teamsPage.isGridLoaded(), "Teams AG-Grid should be loaded.");

        ScreenRecorderUtil.captureFrame(driver, "Navigated to Teams Grid");
        boolean exported = teamsPage.rightClickAndExport();
        Assert.assertTrue(exported, "Teams AG-Grid right-click Excel Export should trigger successfully.");
    }

    @Test(priority = 3, description = "Right-click AG-Grid and export Departments data to Excel")
    public void exportDepartmentsGrid() {
        WebDriver driver = DriverFactory.getDriver();
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.navigateToDepartments();

        TeamsPage deptPage = new TeamsPage(driver);
        Assert.assertTrue(deptPage.isGridLoaded(), "Departments AG-Grid should be loaded.");

        ScreenRecorderUtil.captureFrame(driver, "Navigated to Departments Grid");
        boolean exported = deptPage.rightClickAndExport();
        Assert.assertTrue(exported, "Departments AG-Grid right-click Excel Export should trigger successfully.");
    }

    @Test(priority = 4, description = "Right-click AG-Grid and export Raw Data to Excel")
    public void exportRawDataGrid() {
        WebDriver driver = DriverFactory.getDriver();
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.navigateToRawData();

        RawDataPage rawDataPage = new RawDataPage(driver);
        Assert.assertTrue(rawDataPage.isGridLoaded(), "Raw Data AG-Grid should be loaded.");

        ScreenRecorderUtil.captureFrame(driver, "Navigated to Raw Data Grid");
        boolean exported = rawDataPage.rightClickAndExport();
        Assert.assertTrue(exported, "Raw Data AG-Grid right-click Excel Export should trigger successfully.");
    }

    @Test(priority = 5, description = "Right-click AG-Grid and export Deal Credits data to Excel")
    public void exportDealCreditsGrid() {
        WebDriver driver = DriverFactory.getDriver();
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.navigateToDealCredits();

        DealCreditsPage dealCreditsPage = new DealCreditsPage(driver);
        Assert.assertTrue(dealCreditsPage.isGridLoaded(), "Deal Credits AG-Grid should be loaded.");

        ScreenRecorderUtil.captureFrame(driver, "Navigated to Deal Credits Grid");
        boolean exported = dealCreditsPage.rightClickAndExport();
        Assert.assertTrue(exported, "Deal Credits AG-Grid right-click Excel Export should trigger successfully.");
    }

    @Test(priority = 6, description = "Right-click AG-Grid and export Rate Tables data to Excel")
    public void exportRateTableGrid() {
        WebDriver driver = DriverFactory.getDriver();
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.navigateToRateTables();

        RateTablePage rateTablePage = new RateTablePage(driver);
        Assert.assertTrue(rateTablePage.isGridLoaded(), "Rate Table AG-Grid should be loaded.");

        ScreenRecorderUtil.captureFrame(driver, "Navigated to Rate Table Grid");
        boolean exported = rateTablePage.rightClickAndExport();
        Assert.assertTrue(exported, "Rate Table AG-Grid right-click Excel Export should trigger successfully.");
    }

    @Test(priority = 7, description = "Right-click AG-Grid and export Plans data to Excel")
    public void exportPlansGrid() {
        WebDriver driver = DriverFactory.getDriver();
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.navigateToPlans();

        PlansPage plansPage = new PlansPage(driver);
        Assert.assertTrue(plansPage.isGridLoaded(), "Plans AG-Grid should be loaded.");

        ScreenRecorderUtil.captureFrame(driver, "Navigated to Plans Grid");
        boolean exported = plansPage.rightClickAndExport();
        Assert.assertTrue(exported, "Plans AG-Grid right-click Excel Export should trigger successfully.");
    }

    @Test(priority = 8, description = "Right-click AG-Grid and export Assign Plans data to Excel")
    public void exportAssignPlansGrid() {
        WebDriver driver = DriverFactory.getDriver();
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.navigateToAssignPlans();

        AssignPlansPage assignPlansPage = new AssignPlansPage(driver);
        Assert.assertTrue(assignPlansPage.isGridLoaded(), "Assign Plans AG-Grid should be loaded.");

        ScreenRecorderUtil.captureFrame(driver, "Navigated to Assign Plans Grid");
        boolean exported = assignPlansPage.rightClickAndExport();
        Assert.assertTrue(exported, "Assign Plans AG-Grid right-click Excel Export should trigger successfully.");
    }
}
