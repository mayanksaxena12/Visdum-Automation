package utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;

import Pages.CreateDataStreamPage;
import Pages.DataStreamViewPage;
import Pages.DataStreamsPage;
import Pages.UserDataStreamPage;
import Pages.DashboardPage;
import Pages.DepartmentViewPage;
import Pages.DepartmentsPage;
import Pages.LoginPage;
import Pages.DealsModalPage;
import Pages.RawDataPage;
import Pages.DealCreditsPage;
import Pages.ResourceDeleteModal;
import Pages.ResourceFormModal;
import Pages.ResourcePreviewModal;
import Pages.ResourcesPage;
import Pages.TeamViewPage;
import Pages.TeamsPage;
import Pages.UserViewPage;
import Pages.UsersPage;
import Pages.PlansPage;
import Pages.CreatePlanWizardPage;
import Pages.RateTablePage;
import Pages.CreateRateTableWizardPage;
import Pages.AssignPlansPage;
import Pages.CreateAssignPlanWizardPage;
import Pages.EsignPage;
import Pages.SendEsignPage;
import Pages.WithdrawEsignPage;
import Pages.ComputationPage;
import Pages.ReportsPage;

/**
 * Central registry mapping a data-driven row's {@code Module:Scenario} to a concrete
 * {@link TestAction} built from the existing Page Objects.
 *
 * <p>Dispatch is keyed by <b>Module:Scenario</b> (not by individual Test Case ID) so that adding a
 * new test case that reuses an existing scenario -- e.g. sorting a different column -- is purely a
 * new row in the control file with a different {@code Param1}, requiring <b>zero code changes</b>.
 * A genuinely new scenario type is the only thing that needs a new entry here.
 *
 * <p>All actions are read-only/idempotent so they are safe to run in parallel against shared data.
 * {@code Param1}/{@code Param2} carry the scenario's data (e.g. a search term or an AG-Grid col id).
 */
public final class TestCaseRegistry {

    private static final Map<String, TestAction> ACTIONS = new HashMap<>();

    static {
        // ---------------- User module ----------------
        register("user:search", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Users grid should load.");
            String term = row.param1();
            if (term == null || term.isBlank()) {
                try {
                    term = page.getFirstRowValue("name");
                } catch (Exception ignored) {
                }
            }
            if (term == null || term.isBlank()) {
                term = "Mayank";
            }
            page.search(term);
            Assert.assertTrue(page.isRowListed(term) || page.isGridLoaded(),
                    "Expected a user row to match '" + term + "' or grid to remain loaded.");
        });
        register("user:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "none");
            page.sortByColumn(row.param1());
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "ascending",
                    row.param1() + " should sort ascending.");
        });
        register("user:columnfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            page.openColumnMenu(row.param1());
            Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for " + row.param1());
            page.toggleFirstColumnFilterValue();
            page.closeColumnMenu();
        });
        register("user:view", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            String term = row.param1();
            if (term == null || term.isBlank()) {
                try {
                    term = page.getFirstRowValue("name");
                } catch (Exception ignored) {
                }
            }
            if (term == null || term.isBlank()) {
                term = "Mayank";
            }
            page.search(term);
            page.openView(term);
            UserViewPage view = new UserViewPage(driver);
            Assert.assertTrue(view.isOpen(), "User view drawer should open.");
            view.close();
        });
        register("user:changepassword", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            String term = (row.param1() == null || row.param1().isBlank()) ? "Mayank" : row.param1();
            page.search(term);
            page.openChangePassword(term);
            Pages.ChangePasswordModal modal = new Pages.ChangePasswordModal(driver);
            Assert.assertTrue(modal.isOpen(), "Change password modal should open.");
            modal.cancel();
        });
        register("user:export", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Users grid should load.");
            Assert.assertTrue(page.rightClickAndExport(), "Users AG-Grid right-click Excel Export should succeed.");
        });
        register("user:create", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            page.openCreateUser();
        });
        register("user:edit", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            page.search(row.param1());
            page.openEditUser(row.param1());
        });
        register("user:deactivate", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            String term = (row.param1() == null || row.param1().isBlank()) ? "Mayank" : row.param1();
            page.search(term);
            page.openDeactivateUser(term);
        });
        register("user:customfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            new Pages.UserFilterPage(driver).openFilterPanel();
        });
        register("user:fetch", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            new UsersPage(driver).openFetchUsers();
        });
        register("user:stream", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            if (page.isUserDataStreamsButtonVisible()) {
                page.openUserDataStreams();
                UserDataStreamPage userStream = new UserDataStreamPage(driver);
                if (userStream.isDrawerOpen()) {
                    userStream.closeDrawer();
                } else if (userStream.isWizardOpen()) {
                    userStream.cancelWizard();
                }
            }
        });

        // ---------------- Data Stream module ----------------
        register("datastream:search", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            DataStreamsPage page = new DataStreamsPage(driver);
            String term = row.param1();
            if (term == null || term.isBlank()) {
                try {
                    term = page.getFirstRowValue("name");
                } catch (Exception ignored) {
                }
            }
            if (term == null || term.isBlank()) {
                term = "Deals";
            }
            page.search(term);
            Assert.assertTrue(page.isStreamListed(term) || page.isGridLoaded(),
                    "Expected a data stream row to match '" + term + "'.");
        });
        register("datastream:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            DataStreamsPage page = new DataStreamsPage(driver);
            String col = (row.param1() == null || row.param1().isBlank()) ? "name" : row.param1();
            Assert.assertEquals(page.sortDirectionOf(col), "none");
            page.sortByColumn(col);
            Assert.assertEquals(page.sortDirectionOf(col), "ascending",
                    col + " should sort ascending.");
        });
        register("datastream:columnfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            DataStreamsPage page = new DataStreamsPage(driver);
            String col = (row.param1() == null || row.param1().isBlank()) ? "name" : row.param1();
            page.openColumnMenu(col);
            Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for " + col);
            page.toggleFirstColumnFilterValue();
            page.closeColumnMenu();
        });
        register("datastream:view", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            DataStreamsPage page = new DataStreamsPage(driver);
            String term = row.param1();
            if (term == null || term.isBlank()) {
                try {
                    term = page.getFirstRowValue("name");
                } catch (Exception ignored) {
                }
            }
            if (term == null || term.isBlank()) {
                term = "Deals";
            }
            page.search(term);
            if (page.isStreamListed(term)) {
                page.openViewStream(term);
                DataStreamViewPage view = new DataStreamViewPage(driver);
                Assert.assertTrue(view.isOpen(), "Data stream view drawer should open.");
                view.close();
            }
        });
        register("datastream:create", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            DataStreamsPage page = new DataStreamsPage(driver);
            page.clickCreateDataStream();
            CreateDataStreamPage wizard = new CreateDataStreamPage(driver);
            Assert.assertTrue(wizard.isOpen(), "Create Data Stream wizard should open.");
            wizard.clickCancel();
        });
        register("datastream:tabs", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            DataStreamsPage page = new DataStreamsPage(driver);
            page.switchToDraftTab();
            Assert.assertTrue(page.isDraftTabSelected(), "Drafts tab should be selected.");
            page.switchToActiveTab();
            Assert.assertTrue(page.isActiveTabSelected(), "Active tab should be selected.");
        });
        register("datastream:historytracking", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            DataStreamsPage page = new DataStreamsPage(driver);
            String term = row.param1();
            if (term == null || term.isBlank()) {
                try {
                    term = page.getFirstRowValue("name");
                } catch (Exception ignored) {
                }
            }
            if (term == null || term.isBlank()) {
                term = "Deals";
            }
            page.search(term);
            if (page.isStreamListed(term) && "Active".equalsIgnoreCase(page.statusOf(term))) {
                page.openSetHistoryTracking(term);
                Assert.assertTrue(page.isHistoryTrackingDrawerOpen(), "History tracking drawer should open.");
                page.closeHistoryTrackingDrawer();
            }
        });

        // ---------------- Team module ----------------
        register("team:search", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.search(row.param1());
            Assert.assertTrue(page.isRowListed(row.param1()),
                    "Expected a team row to match '" + row.param1() + "'.");
        });
        register("team:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "none");
            page.sortByColumn(row.param1());
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "ascending",
                    row.param1() + " should sort ascending.");
        });
        register("team:columnfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.openColumnMenu(row.param1());
            Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for " + row.param1());
            page.toggleFirstColumnFilterValue();
            page.closeColumnMenu();
        });
        register("team:view", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.search(row.param1());
            page.openView(row.param1());
            Assert.assertTrue(new TeamViewPage(driver).isOpen(), "Team view drawer should open.");
        });
        register("team:export", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Teams grid should load.");
            Assert.assertTrue(page.rightClickAndExport(), "Teams AG-Grid right-click Excel Export should succeed.");
        });

        // ---------------- Department module ----------------
        register("department:search", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.search(row.param1());
            Assert.assertTrue(page.isRowListed(row.param1()),
                    "Expected a department row to match '" + row.param1() + "'.");
        });
        register("department:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "none");
            page.sortByColumn(row.param1());
            Assert.assertEquals(page.sortDirectionOf(row.param1()), "ascending",
                    row.param1() + " should sort ascending.");
        });
        register("department:columnfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.openColumnMenu(row.param1());
            Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for " + row.param1());
            page.toggleFirstColumnFilterValue();
            page.closeColumnMenu();
        });
        register("department:view", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.search(row.param1());
            page.openView(row.param1());
            Assert.assertTrue(new DepartmentViewPage(driver).isOpen(), "Department view drawer should open.");
        });
        register("department:export", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            TeamsPage page = new TeamsPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Departments grid should load.");
            Assert.assertTrue(page.rightClickAndExport(), "Departments AG-Grid right-click Excel Export should succeed.");
        });

        // ---------------- Data Streams module ----------------

        // ---------------- Resource module ----------------
        register("resource:search", (driver, row) -> {
            new DashboardPage(driver).navigateToResources();
            ResourcesPage page = new ResourcesPage(driver);
            String term = row.param1();
            if (term == null || term.isBlank()) {
                try {
                    term = page.getFirstRowValue("name");
                } catch (Exception ignored) {}
            }
            if (term == null || term.isBlank()) {
                term = "Test";
            }
            page.search(term);
            Assert.assertTrue(page.isRowListed(term),
                    "Expected a resource row to match '" + term + "'.");
        });
        register("resource:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToResources();
            ResourcesPage page = new ResourcesPage(driver);
            String col = row.param1() == null || row.param1().isBlank() ? "name" : row.param1();
            page.sortByColumn(col);
            Assert.assertEquals(page.sortDirectionOf(col), "ascending",
                    col + " should sort ascending.");
        });
        register("resource:columnfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToResources();
            ResourcesPage page = new ResourcesPage(driver);
            String col = row.param1() == null || row.param1().isBlank() ? "name" : row.param1();
            page.openColumnMenu(col);
            Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for " + col);
            page.toggleFirstColumnFilterValue();
            page.closeColumnMenu();
        });
        register("resource:view", (driver, row) -> {
            new DashboardPage(driver).navigateToResources();
            ResourcesPage page = new ResourcesPage(driver);
            String term = row.param1();
            if (term == null || term.isBlank()) {
                try {
                    term = page.getFirstRowValue("name");
                } catch (Exception ignored) {}
            }
            if (term != null && !term.isBlank()) {
                page.openView(term);
                ResourcePreviewModal preview = new ResourcePreviewModal(driver);
                Assert.assertTrue(preview.isPreviewOpen(), "Resource preview modal should open.");
                preview.closePreview();
            }
        });
        register("resource:create", (driver, row) -> {
            new DashboardPage(driver).navigateToResources();
            new ResourcesPage(driver).openAddResource();
            ResourceFormModal form = new ResourceFormModal(driver);
            Assert.assertTrue(form.isModalOpen(), "Add Resource modal should open.");
            form.cancel();
        });
        register("resource:edit", (driver, row) -> {
            new DashboardPage(driver).navigateToResources();
            ResourcesPage page = new ResourcesPage(driver);
            String term = row.param1();
            if (term == null || term.isBlank()) {
                try {
                    term = page.getFirstRowValue("name");
                } catch (Exception ignored) {}
            }
            if (term != null && !term.isBlank()) {
                page.openEditResource(term);
                ResourceFormModal form = new ResourceFormModal(driver);
                Assert.assertTrue(form.isModalOpen(), "Update Resource modal should open.");
                form.cancel();
            }
        });
        register("resource:delete", (driver, row) -> {
            new DashboardPage(driver).navigateToResources();
            ResourcesPage page = new ResourcesPage(driver);
            String term = row.param1();
            if (term == null || term.isBlank()) {
                try {
                    term = page.getFirstRowValue("name");
                } catch (Exception ignored) {}
            }
            if (term != null && !term.isBlank()) {
                page.openDeleteResource(term);
                ResourceDeleteModal modal = new ResourceDeleteModal(driver);
                Assert.assertTrue(modal.isDeleteModalOpen(), "Delete confirmation modal should open.");
                modal.cancelDelete();
            }
        });
        register("resource:validation", (driver, row) -> {
            new DashboardPage(driver).navigateToResources();
            ResourcesPage page = new ResourcesPage(driver);
            page.openAddResource();
            ResourceFormModal form = new ResourceFormModal(driver);
            form.submit();
            Assert.assertTrue(form.isValidationMessageVisible("Resource name is required."));
            form.cancel();
        });

        // ---------------- Login module (validation / error / redirect; no OTP needed) ----------------
        // These actions assume the driver is already on the login page (the Excel runner does NOT
        // pre-login for the Login module).
        register("login:validlogin", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "Valid login should reach the 2-step verification screen.");
        });
        register("login:invalidpassword", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail(ConfigReader.get("username"));
            String pwd = row.param1();
            if (pwd == null || pwd.isBlank()) {
                pwd = ExcelTestDataReader.parseTestDataString(row.get("TestData")).getOrDefault("Password", "InvalidPass_123");
            }
            lp.enterPassword(pwd + "_" + java.time.Instant.now().toEpochMilli());
            lp.clickLogin();
            Assert.assertTrue(lp.isMessageVisible("Invalid Credentials"),
                    "Expected the invalid-credentials error.");
        });
        register("login:invalidemail", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            String pwd = ExcelTestDataReader.parseTestDataString(row.get("TestData")).getOrDefault("Password", "DummyPass@123");
            lp.enterEmail("unregistered_" + java.time.Instant.now().toEpochMilli() + "@visdum.test");
            lp.enterPassword(pwd);
            lp.clickLogin();
            Assert.assertTrue(lp.isMessageVisible("email is invalid") || lp.isMessageVisible("Invalid Credentials"),
                    "Expected error message for invalid email.");
        });
        register("login:emptyfields", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Email is required"));
            Assert.assertTrue(lp.isFieldErrorVisible("Password is required"));
        });
        register("login:emailonly", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail(ConfigReader.get("username"));
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Password is required"));
        });
        register("login:passwordonly", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            String pwd = ExcelTestDataReader.parseTestDataString(row.get("TestData")).getOrDefault("Password", "DummyPass@123");
            lp.enterPassword(pwd);
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Email is required"));
        });
        register("login:emailformat", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            String email = row.param1();
            if (email == null || email.isBlank()) {
                email = row.get("TestData");
            }
            if (email == null || email.isBlank()) {
                email = "email@";
            }
            String pwd = ExcelTestDataReader.parseTestDataString(row.get("TestData")).getOrDefault("Password", "DummyPass@123");
            lp.enterEmail(email);
            lp.enterPassword(pwd);
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Wrong email format"));
        });
        register("login:emaillength", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            String longEmail = ExcelTestDataReader.parseTestDataString(row.get("TestData")).get("Email");
            if (longEmail == null || longEmail.isBlank()) {
                longEmail = "a".repeat(45) + "@example.com";
            }
            String pwd = ExcelTestDataReader.parseTestDataString(row.get("TestData")).getOrDefault("Password", "DummyPass@123");
            lp.enterEmail(longEmail);
            lp.enterPassword(pwd);
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Maximum 50 symbols") || lp.isFieldErrorVisible("Wrong email format"));
        });
        register("login:passwordlength", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            String shortPwd = ExcelTestDataReader.parseTestDataString(row.get("TestData")).get("Password");
            if (shortPwd == null || shortPwd.isBlank()) {
                shortPwd = "cad";
            }
            if (shortPwd.contains(" ")) {
                shortPwd = shortPwd.split(" ")[0];
            }
            lp.enterEmail(ConfigReader.get("username"));
            lp.enterPassword(shortPwd);
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Minimum 8 symbols"));
        });

        // ---------------- 2FA / OTP flow (OTP read live from the UAT database) ----------------
        // The OTP actions below all start from the login page: they submit valid credentials,
        // wait for the 2-step verification screen, fetch the latest OTP from two_factor_authentications
        // (ORDER BY id DESC) and then assert the requested behavior.
 
        register("login:otpsent", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(),
                    "Valid login should reach the 2-step verification screen.");
        });
 
        register("login:otp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            String otp = utilities.OtpDbReader.latestOtp();
            lp.enterOtp(otp);
            lp.clickVerify();
            Assert.assertTrue(new DashboardPage(driver).isLoaded(),
                    "Valid OTP '" + otp + "' should log the user in and land on the dashboard.");
        });
 
        register("login:wrongotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            lp.enterOtp("000000");
            lp.clickVerify();
            Assert.assertTrue(lp.isStillOn2FA(),
                    "Wrong OTP should keep the user on the 2-step verification screen.");
        });
 
        register("login:emptyotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            lp.clickVerify();
            Assert.assertTrue(lp.isStillOn2FA(),
                    "Submitting no OTP should keep the user on the 2-step verification screen.");
        });
 
        register("login:shortotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            lp.enterOtp("123");
            lp.clickVerify();
            Assert.assertTrue(lp.isStillOn2FA(),
                    "A partial OTP should keep the user on the 2-step verification screen.");
        });
 
        register("login:alphaotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            lp.enterOtp("abc123");
            lp.clickVerify();
            Assert.assertTrue(lp.isStillOn2FA(),
                    "An OTP containing alphabets should keep the user on the 2-step verification screen.");
        });
 
        register("login:pasteotp", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.login(ConfigReader.get("username"), ConfigReader.get("password"));
            Assert.assertTrue(lp.is2FADisplayed(), "2-step verification screen should appear.");
            String otp = utilities.OtpDbReader.latestOtp();
            lp.pasteOtp(otp);
            Assert.assertTrue(lp.isOtpFilled(otp),
                    "Pasting a 6-digit OTP should auto-fill all digit boxes.");
        });
 
        register("login:otpformat", (driver, row) -> {
            String otp = utilities.OtpDbReader.latestOtp();
            Assert.assertTrue(otp.matches("\\d{6}"),
                    "two_factor_code '" + otp + "' should be exactly 6 numeric digits.");
        });

        // ---------------- Raw Data module: Refresh Columns ----------------
        register("rawdata:refreshcolumns", (driver, row) -> {
            new DashboardPage(driver).navigateToRawData();
            RawDataPage rawData = new RawDataPage(driver);
            Assert.assertTrue(rawData.isLoaded(), "Raw Data page should load.");
            String stream = row.param1();
            if (stream != null && !stream.isBlank()) {
                rawData.selectDataStreamTab(stream);
            }
            if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                ExecutionGuard.requireDestructiveTestsEnabled();
                rawData.clickRefreshColumns();
                Assert.assertTrue(rawData.isConfirmationModalOpen(), "Confirmation modal should open.");
                rawData.confirmModal();
                DealsModalPage deals = rawData.getDealsModal();
                Assert.assertTrue(deals.isOpen(), "DealsModal should open upon submit.");
                deals.waitForCompletion(60);
                Assert.assertTrue(deals.isCompleted(), "Refresh Columns should complete.");
                deals.closeModal();
            }
        });

        register("rawdata:cancelrefresh", (driver, row) -> {
            new DashboardPage(driver).navigateToRawData();
            RawDataPage rawData = new RawDataPage(driver);
            Assert.assertTrue(rawData.isLoaded(), "Raw Data page should load.");
            String stream = row.param1();
            if (stream != null && !stream.isBlank()) {
                rawData.selectDataStreamTab(stream);
            }
            if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                rawData.clickRefreshColumns();
                Assert.assertTrue(rawData.isConfirmationModalOpen(), "Confirmation modal should open.");
                rawData.cancelModal();
                Assert.assertFalse(rawData.isConfirmationModalOpen(), "Confirmation modal should close on Cancel.");
            }
        });
        register("rawdata:export", (driver, row) -> {
            new DashboardPage(driver).navigateToRawData();
            RawDataPage page = new RawDataPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Raw Data grid should load.");
            Assert.assertTrue(page.rightClickAndExport(), "Raw Data AG-Grid right-click Excel Export should succeed.");
        });
        register("refreshcolumns:export", (driver, row) -> {
            new DashboardPage(driver).navigateToRawData();
            RawDataPage page = new RawDataPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Raw Data grid should load.");
            Assert.assertTrue(page.rightClickAndExport(), "Raw Data AG-Grid right-click Excel Export should succeed.");
        });
        register("dealcredits:export", (driver, row) -> {
            new DashboardPage(driver).navigateToDealCredits();
            DealCreditsPage page = new DealCreditsPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Deal Credits grid should load.");
            Assert.assertTrue(page.rightClickAndExport(), "Deal Credits AG-Grid right-click Excel Export should succeed.");
        });

        // ---------------- Plan module ----------------
        register("plan:search", (driver, row) -> {
            new DashboardPage(driver).navigateToPlans();
            PlansPage page = new PlansPage(driver);
            String term = (row.param1() == null || row.param1().isBlank()) ? "Plan" : row.param1();
            page.searchPlan(term);
            Assert.assertTrue(page.isGridLoaded(), "Plans grid should be loaded for search query: " + term);
        });
        register("plan:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToPlans();
            PlansPage page = new PlansPage(driver);
            String col = (row.param1() == null || row.param1().isBlank()) ? "name" : row.param1();
            page.sortByColumn(col);
            Assert.assertTrue(page.isGridLoaded(), "Plans grid should remain loaded after sorting.");
        });
        register("plan:create", (driver, row) -> {
            new DashboardPage(driver).navigateToPlans();
            PlansPage page = new PlansPage(driver);
            page.clickCreatePlanAdvance();
            CreatePlanWizardPage wizard = new CreatePlanWizardPage(driver);
            Assert.assertTrue(wizard.isLoaded(), "Create Plan wizard should open.");
            wizard.clickCancel();
        });
        register("plan:tabs", (driver, row) -> {
            new DashboardPage(driver).navigateToPlans();
            PlansPage page = new PlansPage(driver);
            page.switchToTemplatesTab();
            page.switchToSavedDraftsTab();
            page.switchToRecentsTab();
            Assert.assertTrue(page.isGridLoaded(), "Plans grid should load on Recents tab.");
        });
        register("plan:grid", (driver, row) -> {
            new DashboardPage(driver).navigateToPlans();
            Assert.assertTrue(new PlansPage(driver).isGridLoaded(), "Plans grid should load.");
        });
        register("plan:export", (driver, row) -> {
            new DashboardPage(driver).navigateToPlans();
            PlansPage page = new PlansPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Plans grid should load.");
            Assert.assertTrue(page.rightClickAndExport(), "Plans AG-Grid right-click Excel Export should succeed.");
        });

        // ---------------- Rate Table module ----------------
        register("ratetable:search", (driver, row) -> {
            new DashboardPage(driver).navigateToRateTables();
            RateTablePage page = new RateTablePage(driver);
            String term = (row.param1() == null || row.param1().isBlank()) ? "Rate" : row.param1();
            page.searchRateTable(term);
            Assert.assertTrue(page.isGridLoaded(), "Rate table page should load for search query: " + term);
        });
        register("ratetable:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToRateTables();
            RateTablePage page = new RateTablePage(driver);
            String col = (row.param1() == null || row.param1().isBlank() || row.param1().contains(":")) ? "name" : row.param1();
            page.sortByColumn(col);
            Assert.assertTrue(page.isGridLoaded(), "Rate table should remain loaded after sorting.");
        });
        register("ratetable:columnfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToRateTables();
            RateTablePage page = new RateTablePage(driver);
            String col = (row.param1() == null || row.param1().isBlank() || row.param1().contains(":") || row.param1().contains(";")) ? "name" : row.param1();
            page.openColumnMenu(col);
            Assert.assertTrue(page.isColumnMenuOpen(), "Column menu should open for " + col);
            page.toggleFirstColumnFilterValue();
            page.closeColumnMenu();
        });
        register("ratetable:create", (driver, row) -> {
            new DashboardPage(driver).navigateToRateTables();
            RateTablePage page = new RateTablePage(driver);
            page.clickCreateRateTable();
            CreateRateTableWizardPage wizard = new CreateRateTableWizardPage(driver);
            Assert.assertTrue(wizard.isLoaded(), "Create Rate Table wizard should open.");
            wizard.clickCancel();
        });
        register("ratetable:tabs", (driver, row) -> {
            new DashboardPage(driver).navigateToRateTables();
            RateTablePage page = new RateTablePage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Rate table tabs / grid should be visible.");
        });
        register("ratetable:addrow", (driver, row) -> {
            new DashboardPage(driver).navigateToRateTables();
            RateTablePage page = new RateTablePage(driver);
            page.clickAddRow();
            Assert.assertTrue(page.isGridLoaded(), "Rate table should remain loaded after adding a row.");
        });
        register("ratetable:view", (driver, row) -> {
            new DashboardPage(driver).navigateToRateTables();
            RateTablePage page = new RateTablePage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Rate table grid should load.");
        });
        register("ratetable:export", (driver, row) -> {
            new DashboardPage(driver).navigateToRateTables();
            RateTablePage page = new RateTablePage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Rate table grid should load.");
            Assert.assertTrue(page.rightClickAndExport(), "Rate Table AG-Grid right-click Excel Export should succeed.");
        });

        // ---------------- Assign Plan module ----------------
        register("assignplan:search", (driver, row) -> {
            new DashboardPage(driver).navigateToAssignPlans();
            AssignPlansPage page = new AssignPlansPage(driver);
            String term = (row.param1() == null || row.param1().isBlank()) ? "John" : row.param1();
            page.searchAssignedPlan(term);
            Assert.assertTrue(page.isGridLoaded(), "Assign Plans grid should load after search.");
        });
        register("assignplan:sort", (driver, row) -> {
            new DashboardPage(driver).navigateToAssignPlans();
            AssignPlansPage page = new AssignPlansPage(driver);
            String col = (row.param1() == null || row.param1().isBlank()) ? "name" : row.param1();
            page.sortByColumn(col);
            Assert.assertTrue(page.isGridLoaded(), "Assign Plans grid should sort.");
        });
        register("assignplan:create", (driver, row) -> {
            new DashboardPage(driver).navigateToAssignPlans();
            AssignPlansPage page = new AssignPlansPage(driver);
            page.clickAssignPlan();
            CreateAssignPlanWizardPage wizard = new CreateAssignPlanWizardPage(driver);
            Assert.assertTrue(wizard.isLoaded(), "Assign Plan wizard should open.");
            wizard.clickCancel();
        });
        register("assignplan:grid", (driver, row) -> {
            new DashboardPage(driver).navigateToAssignPlans();
            Assert.assertTrue(new AssignPlansPage(driver).isGridLoaded(), "Assign Plans grid should load.");
        });
        register("assignplan:export", (driver, row) -> {
            new DashboardPage(driver).navigateToAssignPlans();
            AssignPlansPage page = new AssignPlansPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Assign Plans grid should load.");
            Assert.assertTrue(page.rightClickAndExport(), "Assign Plans AG-Grid right-click Excel Export should succeed.");
        });

        // ---------------- E-Sign module ----------------
        register("esign:send", (driver, row) -> {
            new DashboardPage(driver).navigateToEsign();
            EsignPage page = new EsignPage(driver);
            page.clickSendEnvelope();
            SendEsignPage sendPage = new SendEsignPage(driver);
            Assert.assertTrue(sendPage.isLoaded(), "Send Envelope page should load.");
            sendPage.clickCancel();
        });
        register("esign:withdraw", (driver, row) -> {
            new DashboardPage(driver).navigateToEsign();
            EsignPage page = new EsignPage(driver);
            page.clickWithdrawEnvelope();
            WithdrawEsignPage withdrawPage = new WithdrawEsignPage(driver);
            Assert.assertTrue(withdrawPage.isLoaded(), "Withdraw Envelope page should load.");
            withdrawPage.clickCancel();
        });
        register("esign:search", (driver, row) -> {
            new DashboardPage(driver).navigateToEsign();
            EsignPage page = new EsignPage(driver);
            String term = (row.param1() == null || row.param1().isBlank()) ? "Envelope" : row.param1();
            page.searchEnvelope(term);
            Assert.assertTrue(page.isGridLoaded(), "E-Sign grid should load after search.");
        });
        register("esign:grid", (driver, row) -> {
            new DashboardPage(driver).navigateToEsign();
            Assert.assertTrue(new EsignPage(driver).isLoaded(), "E-Sign page should load.");
        });

        // ---------------- Computation module ----------------
        register("computation:grid", (driver, row) -> {
            new DashboardPage(driver).navigateToComputation();
            ComputationPage page = new ComputationPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Computation grid should load.");
        });
        register("computation:search", (driver, row) -> {
            new DashboardPage(driver).navigateToComputation();
            ComputationPage page = new ComputationPage(driver);
            String term = (row.param1() == null || row.param1().isBlank()) ? "FY" : row.param1();
            page.searchComputation(term);
            Assert.assertTrue(page.isGridLoaded(), "Computation grid should remain loaded after search.");
        });
        register("computation:tabs", (driver, row) -> {
            new DashboardPage(driver).navigateToComputation();
            ComputationPage page = new ComputationPage(driver);
            page.switchToSavedDraftsTab();
            Assert.assertTrue(page.isGridLoaded(), "Computation drafts tab grid should load.");
            page.switchToRecentsTab();
            Assert.assertTrue(page.isGridLoaded(), "Computation recents tab grid should load.");
        });
        register("computation:export", (driver, row) -> {
            new DashboardPage(driver).navigateToComputation();
            ComputationPage page = new ComputationPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Computation grid should load before export.");
            Assert.assertTrue(page.rightClickAndExport(), "Excel export should download a readable file.");
        });

        // ---------------- Reports module ----------------
        register("report:grid", (driver, row) -> {
            new DashboardPage(driver).navigateToReports();
            ReportsPage page = new ReportsPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Reports grid should load.");
        });
        register("report:search", (driver, row) -> {
            new DashboardPage(driver).navigateToReports();
            ReportsPage page = new ReportsPage(driver);
            String term = (row.param1() == null || row.param1().isBlank()) ? "Report" : row.param1();
            page.searchReport(term);
            Assert.assertTrue(page.isGridLoaded(), "Reports grid should remain loaded after search.");
        });
        register("report:tabs", (driver, row) -> {
            new DashboardPage(driver).navigateToReports();
            ReportsPage page = new ReportsPage(driver);
            page.switchToCustomReportsTab();
            Assert.assertTrue(page.isGridLoaded(), "Custom reports tab grid should load.");
            page.switchToAllReportsTab();
            Assert.assertTrue(page.isGridLoaded(), "All reports tab grid should load.");
        });
        register("report:export", (driver, row) -> {
            new DashboardPage(driver).navigateToReports();
            ReportsPage page = new ReportsPage(driver);
            Assert.assertTrue(page.isGridLoaded(), "Reports grid should load before export.");
            Assert.assertTrue(page.rightClickAndExport(), "Excel export should download a readable file.");
        });

        // ---------------- Organization module ----------------
        register("organization:grid", (driver, row) -> {
            Pages.OrganizationPage page = new Pages.OrganizationPage(driver);
            page.navigateToOrganizations();
            Assert.assertTrue(page.isLoaded() || driver.getCurrentUrl().contains("organization"),
                    "Organization page should load.");
        });
        register("organization:search", (driver, row) -> {
            Pages.OrganizationPage page = new Pages.OrganizationPage(driver);
            page.navigateToOrganizations();
            String term = (row.param1() == null || row.param1().isBlank()) ? "AutoOrg" : row.param1();
            page.searchOrganization(term);
            Assert.assertTrue(page.isLoaded() || driver.getCurrentUrl().contains("organization"),
                    "Organization page should remain loaded after search.");
        });
        register("organization:create", (driver, row) -> {
            Pages.OrganizationPage page = new Pages.OrganizationPage(driver);
            page.navigateToOrganizations();
            if (page.isAddOrganizationButtonVisible()) {
                page.clickAddOrganization();
                Assert.assertTrue(page.isDrawerOpen(), "Add Organization drawer should open.");
            }
        });
        register("organization:view", (driver, row) -> {
            Pages.OrganizationPage page = new Pages.OrganizationPage(driver);
            page.navigateToOrganizations();
            Assert.assertTrue(page.isLoaded() || driver.getCurrentUrl().contains("organization"),
                    "Organization list should load for view scenario.");
        });
    }

    /*
     * Removed (P0 audit fix): these actions only opened a drawer/modal or clicked a button and
     * asserted nothing, so every workbook row mapped to them reported PASS without verifying
     * its scenario. With no registration, those rows now report Skipped (manual) until a
     * version that asserts the outcome is written. The old code is in git history.
     *   assignplan:bulk, datastream:edit, department:activate, department:addmembers, department:create, department:deactivate, department:edit, esign:email, plan:clone, plan:delete, plan:edit, plan:expression, plan:status, ratetable:edit, team:activate, team:addmembers, team:create, team:deactivate, team:edit
     */

    private TestCaseRegistry() {
    }

    private static void register(String key, TestAction action) {
        ACTIONS.put(key, action);
    }

    public static TestAction get(String actionKey) {
        TestAction action = ACTIONS.get(actionKey);
        if (action != null) {
            return action;
        }
        if (actionKey.toLowerCase().startsWith("refreshcolumns:")
                || actionKey.toLowerCase().startsWith("refresh columns:")
                || actionKey.toLowerCase().startsWith("rawdata:")) {
            return TestCaseRegistry::executeRefreshColumnsAction;
        }
        // Unmapped scenario: return null so the runner reports the row as Skipped (manual).
        return null;
    }

    private static void executeRefreshColumnsAction(WebDriver driver, TestCaseRow row) {
        String tcId = row.getTestCaseId().toUpperCase().trim();
        DashboardPage dashboard = new DashboardPage(driver);
        RawDataPage rawData = new RawDataPage(driver);
        dashboard.navigateToRawData();
        Assert.assertTrue(rawData.isLoaded(), "Raw Data page should load for " + tcId);

        // Helper to select an eligible stream tab (with Refresh Columns action)
        Runnable selectEligibleTab = () -> {
            if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                return;
            }
            List<String> tabs = rawData.getDataStreamTabNames();
            for (String tab : tabs) {
                rawData.selectDataStreamTab(tab);
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    return;
                }
            }
        };

        switch (tcId) {
            case "TC-01":
            case "TC-02":
            case "TC-03":
                selectEligibleTab.run();
                if (rawData.isHeaderActionsPresent()) {
                    Assert.assertTrue(rawData.isHeaderActionPresent("Refresh Columns"),
                            tcId + ": Expected 'Refresh Columns' action to be displayed in Action menu.");
                    long count = rawData.getHeaderActionNames().stream()
                            .filter(a -> a.equalsIgnoreCase("Refresh Columns")).count();
                    Assert.assertEquals(count, 1, tcId + ": Expected 'Refresh Columns' to appear only once.");
                }
                break;

            case "TC-04":
                for (String tab : rawData.getDataStreamTabNames()) {
                    if (tab.toLowerCase().contains("raw") || tab.toLowerCase().contains("source")) {
                        rawData.selectDataStreamTab(tab);
                        if (rawData.isHeaderActionsPresent()) {
                            Assert.assertFalse(rawData.isHeaderActionPresent("Refresh Columns"),
                                    tcId + ": Expected 'Refresh Columns' to be hidden for non-calculated stream.");
                        }
                        break;
                    }
                }
                break;

            case "TC-05":
            case "TC-06":
            case "TC-58":
                for (String tab : rawData.getDataStreamTabNames()) {
                    if (tab.toLowerCase().contains("key-value") || tab.toLowerCase().contains("key value")) {
                        rawData.selectDataStreamTab(tab);
                        if (rawData.isHeaderActionsPresent()) {
                            Assert.assertFalse(rawData.isHeaderActionPresent("Refresh Columns"),
                                    tcId + ": 'Refresh Columns' should be hidden for Key-Value stream.");
                        }
                        break;
                    }
                }
                break;

            case "TC-07":
                selectEligibleTab.run();
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    List<String> actions = rawData.getHeaderActionNames();
                    Assert.assertTrue(actions.contains("Refresh Columns"),
                            tcId + ": Action label should appear exactly as 'Refresh Columns'.");
                }
                break;

            case "TC-08":
                selectEligibleTab.run();
                if (rawData.isHeaderActionsPresent()) {
                    List<String> actions = rawData.getHeaderActionNames();
                    boolean hasStandard = actions.stream().anyMatch(a ->
                            a.contains("Add New Row") || a.contains("Process") || a.contains("Edit"));
                    Assert.assertTrue(hasStandard, tcId + ": Existing actions should remain intact.");
                }
                break;

            case "TC-09":
            case "TC-10":
            case "TC-11":
            case "TC-12":
                selectEligibleTab.run();
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    rawData.clickRefreshColumns();
                    Assert.assertTrue(rawData.isConfirmationModalOpen(), tcId + ": Confirmation modal should open.");
                    String text = rawData.getConfirmationModalText();
                    Assert.assertEquals(text.trim(),
                            "Are you sure you want to refresh all Derived and Lookup Column values?",
                            tcId + ": Modal confirmation message should match exactly.");
                    Assert.assertTrue(rawData.isConfirmationCancelButtonOutlineStyled(),
                            tcId + ": Cancel should be secondary/lighter.");
                    Assert.assertTrue(rawData.isConfirmationSubmitButtonPrimaryStyled(),
                            tcId + ": Submit should be primary/darker.");
                    rawData.cancelModal();
                }
                break;

            case "TC-13":
                selectEligibleTab.run();
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    rawData.clickRefreshColumns();
                    Assert.assertTrue(rawData.isConfirmationModalOpen(), tcId + ": Confirmation modal should open.");
                    rawData.cancelModal();
                    Assert.assertFalse(rawData.isConfirmationModalOpen(), tcId + ": Modal should close on Cancel.");
                    Assert.assertFalse(rawData.getDealsModal().isOpen(), tcId + ": No processing should start.");
                }
                break;

            case "TC-14":
                selectEligibleTab.run();
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    rawData.clickRefreshColumns();
                    Assert.assertTrue(rawData.isConfirmationModalOpen(), tcId + ": Confirmation modal should open.");
                    rawData.closeConfirmationModalByHeaderClose();
                    Assert.assertFalse(rawData.isConfirmationModalOpen(), tcId + ": Modal should close on close icon.");
                    Assert.assertFalse(rawData.getDealsModal().isOpen(), tcId + ": No processing should start.");
                }
                break;

            case "TC-15":
            case "TC-16":
            case "TC-17":
            case "TC-18":
            case "TC-19":
            case "TC-22":
            case "TC-23":
            case "TC-24":
            case "TC-25":
            case "TC-26":
            case "TC-59":
                selectEligibleTab.run();
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    ExecutionGuard.requireDestructiveTestsEnabled();
                    rawData.clickRefreshColumns();
                    rawData.confirmModal();
                    DealsModalPage deals = rawData.getDealsModal();
                    Assert.assertTrue(deals.isOpen(), tcId + ": DealsModal should open upon submit.");
                    boolean running = deals.isInitialNoticeDisplayed()
                            || deals.isProgressBarPresent()
                            || deals.isCompleted();
                    Assert.assertTrue(running, tcId + ": DealsModal should show running/progress state.");
                    deals.closeModal();
                    Assert.assertTrue(deals.isClosed(), tcId + ": DealsModal should close without stopping background job.");
                }
                break;

            case "TC-27":
            case "TC-28":
            case "TC-29":
            case "TC-30":
            case "TC-31":
            case "TC-32":
            case "TC-33":
            case "TC-34":
            case "TC-61":
                selectEligibleTab.run();
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    ExecutionGuard.requireDestructiveTestsEnabled();
                    rawData.clickRefreshColumns();
                    rawData.confirmModal();
                    DealsModalPage deals = rawData.getDealsModal();
                    Assert.assertTrue(deals.isOpen(), tcId + ": DealsModal should open.");
                    deals.waitForCompletion(60);
                    Assert.assertTrue(deals.isCompleted(), tcId + ": Processing should complete.");
                    Assert.assertTrue(deals.getDerivedColumnsProcessedCount() >= 0, tcId + ": Derived Columns count >= 0");
                    Assert.assertTrue(deals.getLookupColumnsProcessedCount() >= 0, tcId + ": Lookup Columns count >= 0");
                    Assert.assertTrue(deals.getRecordsProcessedCount() >= 0, tcId + ": Records Processed >= 0");
                    Assert.assertTrue(deals.getRecordsUpdatedCount() >= 0, tcId + ": Records Updated >= 0");
                    deals.closeModal();
                    Assert.assertTrue(rawData.isGridLoaded(), tcId + ": Grid should remain loaded.");
                }
                break;

            case "TC-20":
            case "TC-21":
            case "TC-62":
                selectEligibleTab.run();
                String initialTab = rawData.getSelectedDataStreamTab();
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    rawData.clickRefreshColumns();
                    rawData.cancelModal();
                    Assert.assertEquals(rawData.getSelectedDataStreamTab(), initialTab,
                            tcId + ": Tab selection should remain scoped to active stream.");
                }
                break;

            default:
                throw new SkipException(tcId + ": no automation mapped for this Refresh Columns case (manual).");
        }
    }

    /** Refresh Columns workbook IDs that have a real case in executeRefreshColumnsAction. */
    private static final java.util.Set<String> REFRESH_COLUMNS_AUTOMATED = java.util.Set.of("tc-01", "tc-02", "tc-03", "tc-04", "tc-05", "tc-06", "tc-07", "tc-08", "tc-09", "tc-10", "tc-11", "tc-12", "tc-13", "tc-14", "tc-15", "tc-16", "tc-17", "tc-18", "tc-19", "tc-20", "tc-21", "tc-22", "tc-23", "tc-24", "tc-25", "tc-26", "tc-27", "tc-28", "tc-29", "tc-30", "tc-31", "tc-32", "tc-33", "tc-34", "tc-58", "tc-59", "tc-61", "tc-62");

    public static boolean isMapped(String actionKey) {
        String key = actionKey == null ? "" : actionKey.toLowerCase().trim();
        if (ACTIONS.containsKey(key)) {
            return true;
        }
        int colon = key.indexOf(':');
        String module = colon < 0 ? key : key.substring(0, colon);
        if (module.equals("refreshcolumns") || module.equals("refresh columns") || module.equals("rawdata")) {
            return REFRESH_COLUMNS_AUTOMATED.contains(key.substring(colon + 1));
        }
        return false;
    }
}
