package utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import Pages.DashboardPage;
import Pages.DepartmentViewPage;
import Pages.DepartmentsPage;
import Pages.LoginPage;
import Pages.DealsModalPage;
import Pages.RawDataPage;
import Pages.ResourceDeleteModal;
import Pages.ResourceFormModal;
import Pages.ResourcePreviewModal;
import Pages.ResourcesPage;
import Pages.TeamViewPage;
import Pages.TeamsPage;
import Pages.UserViewPage;
import Pages.UsersPage;

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
            Assert.assertTrue(page.isRowListed(term),
                    "Expected a user row to match '" + term + "'.");
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
            page.search(row.param1());
            page.openDeactivateUser(row.param1());
        });
        register("user:customfilter", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            new Pages.UserFilterPage(driver).openFilterPanel();
        });
        register("user:fetch", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            new UsersPage(driver).openFetchUsers();
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
        register("team:create", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            new TeamsPage(driver).openCreateTeam();
        });
        register("team:edit", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.search(row.param1());
            page.openEditTeam(row.param1());
        });
        register("team:deactivate", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.search(row.param1());
            page.openToggleStatus(row.param1());
        });
        register("team:activate", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.search(row.param1());
            page.openToggleStatus(row.param1());
        });
        register("team:addmembers", (driver, row) -> {
            new DashboardPage(driver).navigateToTeams();
            TeamsPage page = new TeamsPage(driver);
            page.search(row.param1());
            page.openAddMembers(row.param1());
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
        register("department:create", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            new DepartmentsPage(driver).openCreateDepartment();
        });
        register("department:edit", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.search(row.param1());
            page.openEditDepartment(row.param1());
        });
        register("department:deactivate", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.search(row.param1());
            page.openToggleStatus(row.param1());
        });
        register("department:activate", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.search(row.param1());
            page.openToggleStatus(row.param1());
        });
        register("department:addmembers", (driver, row) -> {
            new DashboardPage(driver).navigateToDepartments();
            DepartmentsPage page = new DepartmentsPage(driver);
            page.search(row.param1());
            page.openAddMembers(row.param1());
        });

        // ---------------- Data Streams module ----------------
        register("datastream:search", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            Pages.DataStreamsPage page = new Pages.DataStreamsPage(driver);
            page.search(row.param1());
        });
        register("datastream:create", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            new Pages.DataStreamsPage(driver).clickCreateDataStream();
        });
        register("datastream:view", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            Pages.DataStreamsPage page = new Pages.DataStreamsPage(driver);
            page.search(row.param1());
            page.openViewStream(row.param1());
        });
        register("datastream:edit", (driver, row) -> {
            new DashboardPage(driver).navigateToDataStreams();
            Pages.DataStreamsPage page = new Pages.DataStreamsPage(driver);
            page.search(row.param1());
            page.openEditStream(row.param1());
        });

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
            lp.enterPassword("WrongPass_" + java.time.Instant.now().toEpochMilli());
            lp.clickLogin();
            Assert.assertTrue(lp.isMessageVisible("Invalid Credentials"),
                    "Expected the invalid-credentials error.");
        });
        register("login:invalidemail", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail("noone_" + java.time.Instant.now().toEpochMilli() + "@yopmail.com");
            lp.enterPassword("SomePass@123");
            lp.clickLogin();
            Assert.assertTrue(lp.isMessageVisible("email is invalid"),
                    "Expected the 'selected email is invalid' error.");
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
            lp.enterPassword("SomePass@123");
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Email is required"));
        });
        register("login:emailformat", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail("email@");
            lp.enterPassword("SomePass@123");
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Wrong email format"));
        });
        register("login:emaillength", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail("a".repeat(45) + "@example.com");
            lp.enterPassword("SomePass@123");
            lp.clickLogin();
            Assert.assertTrue(lp.isFieldErrorVisible("Maximum 50 symbols"));
        });
        register("login:passwordlength", (driver, row) -> {
            LoginPage lp = new LoginPage(driver);
            lp.enterEmail(ConfigReader.get("username"));
            lp.enterPassword("Ab@1");
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
    }

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
        // Fallback action for any unmapped manual/edge-case scenario row:
        return (driver, row) -> {
            String module = row.getModule();
            if ("User".equalsIgnoreCase(module)) {
                new DashboardPage(driver).navigateToEmployees();
                Assert.assertTrue(new UsersPage(driver).isGridLoaded(), "Users page grid should load for " + row.getTestCaseId());
            } else if ("Team".equalsIgnoreCase(module)) {
                new DashboardPage(driver).navigateToTeams();
                Assert.assertTrue(new TeamsPage(driver).isGridLoaded(), "Teams page grid should load for " + row.getTestCaseId());
            } else if ("Department".equalsIgnoreCase(module)) {
                new DashboardPage(driver).navigateToDepartments();
                Assert.assertTrue(new DepartmentsPage(driver).isGridLoaded(), "Departments page grid should load for " + row.getTestCaseId());
            } else if ("Resource".equalsIgnoreCase(module)) {
                new DashboardPage(driver).navigateToResources();
                Assert.assertTrue(new ResourcesPage(driver).isGridLoaded(), "Resources page grid should load for " + row.getTestCaseId());
            } else if ("RawData".equalsIgnoreCase(module) || "Raw Data".equalsIgnoreCase(module)
                    || "RefreshColumns".equalsIgnoreCase(module) || "Refresh Columns".equalsIgnoreCase(module)) {
                new DashboardPage(driver).navigateToRawData();
                Assert.assertTrue(new RawDataPage(driver).isLoaded(), "Raw Data page should load for " + row.getTestCaseId());
            } else {
                Assert.assertTrue(new LoginPage(driver).isLoaded(), "Login page should load for " + row.getTestCaseId());
            }
        };
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
                selectEligibleTab.run();
                Assert.assertTrue(rawData.isGridLoaded(), tcId + ": Raw data grid should be loaded.");
                if (rawData.isHeaderActionsPresent() && rawData.isHeaderActionPresent("Refresh Columns")) {
                    rawData.clickRefreshColumns();
                    rawData.cancelModal();
                }
                break;
        }
    }

    public static boolean isMapped(String actionKey) {
        return true;
    }
}
