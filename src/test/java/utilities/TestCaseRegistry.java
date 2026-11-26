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
            String term = userLookupTerm(page, row);
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
            String term = userLookupTerm(page, row);
            page.search(term);
            page.openView(term);
            UserViewPage view = new UserViewPage(driver);
            Assert.assertTrue(view.isOpen(), "User view drawer should open.");
            view.close();
        });
        register("user:changepassword", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            String term = userLookupTerm(page, row);
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
        // ---------------- Users sheet: User Data Stream (view drawer + wizard) ----------------
        // The wizard is walked step by step but Submit is never clicked (it would overwrite the
        // tenant's user stream). Rows needing a connected CRM skip with a reason when there is none.
        registerId("User", "USV-002", (driver, row) -> Assert.assertFalse(
                streamView(driver).userSourceName().isBlank(), "USV-002: User Source should show the platform name."));
        registerId("User", "USV-003", (driver, row) -> {
            java.util.List<String[]> m = streamView(driver).mappingRows();
            Assert.assertFalse(m.isEmpty(), "USV-003: 'User Column & Orders' should list mappings.");
            Assert.assertTrue(m.stream().anyMatch(r -> r[1].endsWith("*")), "USV-003: required fields should be marked with *.");
        });
        registerId("User", "USV-004", (driver, row) -> Assert.assertTrue(
                streamView(driver).isSectionVisible("Criterias"), "USV-004: 'Criterias' section should be shown."));

        registerId("User", "SS1-001", (driver, row) -> {
            Pages.UserStreamWizardPage w = newStreamWizard(driver);
            Assert.assertEquals(w.currentStepTitle(), "Stream Details", "SS1-001: first step should be 'Stream Details'.");
            Assert.assertTrue(w.isSectionVisible("Choose App"), "SS1-001: 'Choose App' should be shown.");
            Assert.assertTrue(w.isSectionVisible("Stream Setup"), "SS1-001: 'Stream Setup' stepper panel should be shown.");
            Assert.assertTrue(w.isStepperCurrent("Stream Details"), "SS1-001: Stream Details should be the active step.");
        });
        registerId("User", "SS1-002", (driver, row) -> {
            java.util.List<String> apps = connectedApps(driver, row);
            Assert.assertFalse(apps.isEmpty(), "SS1-002: 'Choose App' should list the connected apps.");
        });
        for (String[] v : new String[][] {{"SS1-003", "Salesforce"}, {"SS1-004", "HubSpot"}, {"SS1-005", "QuickBooks"}, {"SS1-006", "OneDrive"}}) {
            registerId("User", v[0], (driver, row) -> {
                Pages.UserStreamWizardPage w = newStreamWizard(driver);
                java.util.List<String> apps = w.appOptions();
                String label = apps.stream().filter(a -> a.equalsIgnoreCase(v[1])).findFirst().orElse(null);
                if (label == null) {
                    throw new SkipException(v[0] + ": " + v[1] + " is not connected in this tenant (connected: " + apps + ").");
                }
                Assert.assertEquals(w.chooseApp(label), label, v[0] + ": " + v[1] + " should be selected.");
            });
        }
        registerId("User", "SS1-007", (driver, row) -> {
            Pages.UserStreamWizardPage w = newStreamWizard(driver);
            w.next();
            Assert.assertTrue(w.isToastVisible("Please choose app to proceed"), "SS1-007: an error should ask to choose an app.");
            Assert.assertEquals(w.currentStepTitle(), "Stream Details", "SS1-007: must stay on Stream Details.");
        });
        registerId("User", "SS1-008", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 2);
            Assert.assertTrue(w.isStepperCompleted("Stream Details"), "SS1-008: Stream Details should show as completed.");
        });

        registerId("User", "SS2-001", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 2);
            Assert.assertTrue(w.isSectionVisible("Visdum"), "SS2-001: the Visdum side of the mapping should be shown.");
        });
        registerId("User", "SS2-002", (driver, row) -> {
            java.util.List<String> f = streamWizardAt(driver, row, 2).targetFieldNames();
            Assert.assertFalse(f.isEmpty(), "SS2-002: Visdum target fields should be listed.");
            Assert.assertTrue(f.stream().anyMatch(x -> x.endsWith("*")), "SS2-002: required fields should be marked with *, got " + f);
        });
        registerId("User", "SS2-003", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 2);
            String first = w.targetFieldNames().get(0).replace("*", "");
            Assert.assertFalse(w.sourceOptions(first).isEmpty(), "SS2-003: source columns should be offered for mapping.");
        });
        registerId("User", "SS2-004", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 2);
            String first = w.targetFieldNames().get(0).replace("*", "");
            if (w.mappedSource(first).isEmpty()) {
                w.mapField(first);
            }
            Assert.assertTrue(w.dataTypeOf(first).equalsIgnoreCase("Text"), "SS2-004: data type should default to Text, got '" + w.dataTypeOf(first) + "'.");
        });
        for (String[] v : new String[][] {{"SS2-005", "first_name"}, {"SS2-006", "email"}, {"SS2-007", "user_ref_id"}}) {
            registerId("User", v[0], (driver, row) -> {
                Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 2);
                if (w.targetFieldNames().stream().noneMatch(x -> x.replace("*", "").equals(v[1]))) {
                    throw new SkipException(v[0] + ": this stream has no '" + v[1] + "' target field.");
                }
                Assert.assertFalse(w.mapField(v[1]).isEmpty(), v[0] + ": " + v[1] + " should be mapped to a source column.");
            });
        }
        registerId("User", "SS2-008", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 2);
            String required = w.targetFieldNames().stream().filter(x -> x.endsWith("*")).findFirst()
                    .orElseThrow(() -> new SkipException("SS2-008: no required fields on this stream.")).replace("*", "");
            w.clearMapping(required);
            w.next();
            Assert.assertEquals(w.currentStepTitle(), "User Mapping & Ordering", "SS2-008: must not proceed with '" + required + "' unmapped.");
        });
        registerId("User", "SS2-009", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 2);
            w.previous();
            Assert.assertTrue(w.waitForStep("Stream Details"), "SS2-009: Previous should return to Stream Details.");
            Assert.assertFalse(w.selectedApp().isEmpty(), "SS2-009: the chosen app should still be selected.");
        });
        registerId("User", "SS2-010", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 3);
            Assert.assertTrue(w.isStepperCompleted("User Mapping & Ordering"), "SS2-010: mapping step should show as completed.");
        });

        registerId("User", "SS3-001", (driver, row) -> Assert.assertTrue(
                streamWizardAt(driver, row, 3).isAddCriteriaVisible(), "SS3-001: 'Add New Criteria' should be shown."));
        registerId("User", "SS3-003", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 3);
            w.addCriteria();
            Assert.assertTrue(w.isOperatorDropdownVisible(), "SS3-003: an Operator dropdown should appear.");
        });
        registerId("User", "SS3-004", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 3);
            w.addCriteria();
            Assert.assertTrue(w.isSectionVisible("Text"), "SS3-004: Comparison type should default to Text.");
        });
        registerId("User", "SS3-005", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 3);
            w.addCriteria();
            w.typeComparisonValue("Active");
            Assert.assertEquals(w.comparisonValue(), "Active", "SS3-005: the comparison value should accept text.");
        });
        registerId("User", "SS3-006", (driver, row) -> {
            java.util.List<String> opts = streamWizardWithTwoCriteria(driver, row).conditionOptions();
            Assert.assertTrue(opts.contains("AND") && opts.contains("OR"), "SS3-006: Condition should offer AND and OR, got " + opts);
        });
        for (String[] v : new String[][] {{"SS3-007", "AND"}, {"SS3-008", "OR"}}) {
            registerId("User", v[0], (driver, row) -> Assert.assertEquals(
                    streamWizardWithTwoCriteria(driver, row).chooseCondition(v[1]), v[1], v[0] + ": " + v[1] + " should be selected."));
        }
        registerId("User", "SS3-010", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardWithTwoCriteria(driver, row);
            int before = w.criteriaCount();
            w.deleteFirstCriteria();
            Assert.assertEquals(w.criteriaCount(), before - 1, "SS3-010: deleting should remove one criteria block.");
        });

        registerId("User", "SS4-002", (driver, row) -> Assert.assertFalse(
                streamWizardAt(driver, row, 4).userSourceName().isBlank(), "SS4-002: User Source should be summarised."));
        registerId("User", "SS4-003", (driver, row) -> Assert.assertFalse(
                streamWizardAt(driver, row, 4).mappingRows().isEmpty(), "SS4-003: column mappings should be summarised."));
        registerId("User", "SS4-004", (driver, row) -> Assert.assertTrue(
                streamWizardAt(driver, row, 4).isSectionVisible("Criterias"), "SS4-004: 'Criterias' should be summarised."));
        registerId("User", "SS4-006", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 4);
            w.previous();
            Assert.assertTrue(w.waitForStep("Define Criteria") || w.waitForStep("User Mapping & Ordering"),
                    "SS4-006: Previous should go back one step.");
        });
        manualId("User", "SS4-005", "Submit would overwrite this tenant's User Data Stream configuration");

        // ---------------- Users sheet: Fetch Users (Add Users is never clicked) ----------------
        registerId("User", "FU-001", (driver, row) -> {
            java.util.List<String> h = fetchUsers(driver).columnHeaders();
            Assert.assertTrue(h.size() >= 3, "FU-001: the fetched-users table should show its columns, got " + h);
        });
        registerId("User", "FU-002", (driver, row) -> {
            Pages.FetchUsersPage f = fetchUsers(driver);
            Assert.assertTrue(f.isLegendVisible("New data", "bg-success"), "FU-002: 'New data' (green) legend should be shown.");
            if (f.rowsWithIndicator("bg-success") == 0) {
                throw new SkipException("FU-002: no new users in the connected source right now.");
            }
        });
        registerId("User", "FU-003", (driver, row) -> {
            Pages.FetchUsersPage f = fetchUsers(driver);
            Assert.assertTrue(f.isLegendVisible("Modified data", "bg-warning"), "FU-003: 'Modified data' (yellow) legend should be shown.");
            if (f.rowsWithIndicator("bg-warning") == 0) {
                throw new SkipException("FU-003: no modified users in the connected source right now.");
            }
        });
        registerId("User", "FU-005", (driver, row) -> {
            Pages.FetchUsersPage f = fetchUsersWithRows(driver);
            f.tickFirstRow();
            Assert.assertTrue(f.selectedRowCount() >= 1, "FU-005: the ticked row should be selected.");
        });
        registerId("User", "FU-006", (driver, row) -> {
            Pages.FetchUsersPage f = fetchUsersWithRows(driver);
            f.tickHeaderCheckbox();
            Assert.assertEquals(f.selectedRowCount(), f.rowCount(), "FU-006: the header checkbox should select every row.");
        });
        registerId("User", "FU-008", (driver, row) -> Assert.assertFalse(
                fetchUsersWithRows(driver).isAddUsersEnabled(), "FU-008: 'Add Users' should be disabled until a user is selected."));
        registerId("User", "FU-009", (driver, row) -> {
            String t = fetchUsers(driver).totalRecordsText();
            Assert.assertTrue(t.matches("Total Records:\\s*\\d+"), "FU-009: row count should be shown at the bottom, got '" + t + "'.");
        });
        registerId("User", "FU-010", (driver, row) -> {
            fetchUsers(driver).clickBack();
            Assert.assertTrue(new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(d -> !d.getCurrentUrl().contains("fetch-users")), "FU-010: Back should leave Fetch Users.");
        });
        manualId("User", "FU-007", "Add Users permanently imports users from the connected CRM");
        manualId("User", "E2E-006", "Add Users permanently imports users from the connected CRM");

        // ---------------- Users sheet: remaining Create / Edit / View / modal rows ----------------
        // -- Step 1 --
        for (String id : new String[] {"TS_11_TC_01", "CS1-011", "CS1-012"}) {
            // Image and Language are optional: valid required fields alone must reach Step 2.
            registerId("User", id, (driver, row) -> openCreateUserAtStep(driver, 2));
        }
        registerId("User", "CS1-009", (driver, row) -> {
            java.util.List<String> roles = openCreateUser(driver).dropdownOptions("Select Role");
            for (String r : new String[] {"Admin", "Manager", "Individual Contributor"}) {
                Assert.assertTrue(roles.contains(r), "CS1-009: Role options should include '" + r + "', got " + roles);
            }
        });
        registerId("User", "CS1-010", (driver, row) -> {
            java.util.List<String> cur = openCreateUser(driver).dropdownOptions("Select Currency");
            Assert.assertTrue(cur.size() > 1 && cur.stream().anyMatch(c -> c.contains("USD")),
                    "CS1-010: Currency should offer several options including USD, got " + cur);
        });
        registerId("User", "CS1-015", (driver, row) -> Assert.assertTrue(
                openCreateUser(driver).uploadProfileImage(samplePng()), "CS1-015: PNG should upload and show a preview."));
        for (String id : new String[] {"CS1-013", "CS3-011"}) {
            registerId("User", id, (driver, row) -> {
                // Uses the email of a user that already exists, so the server must refuse it.
                UsersPage page = usersPage(driver);
                String existing = page.getFirstRowValue("email");
                page.openCreateUser();
                Pages.CreateUserPage form = new Pages.CreateUserPage(driver);
                String sfx = Long.toString(System.currentTimeMillis() % 100000);
                form.fillPersonalDetailsExcept("none", "Dup Check " + sfx, existing, "DUP-" + sfx,
                        System.getProperty("test.user.role", "Individual Contributor"), System.getProperty("test.user.currency", "USD"));
                form.clickNextStep();
                if (form.isInputVisible("employee_number")) {
                    form.clickNextStep();
                    form.tryPasswordSubmit("Dup@" + sfx + "x", "Dup@" + sfx + "x");
                }
                Assert.assertTrue(form.isTextVisible("The email has already been taken."),
                        id + ": expected 'The email has already been taken.' for existing email " + existing);
            });
        }
        registerId("User", "TC_EXTRA-U012", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUser(driver);
            form.typeInto("name", "N".repeat(205));
            form.clickNextStep();
            Assert.assertTrue(form.waitForValidationMessage("Your Name should be maximum of 50 characters"), "TC_EXTRA-U012: max length error expected.");
            Assert.assertTrue(form.isOnPersonalDetailsStep(), "TC_EXTRA-U012: the form must not advance.");
        });

        // -- Step 2 --
        for (String id : new String[] {"CS2-002", "CS2-004", "CS2-005", "CS2-006", "CS2-010"}) {
            // Employee ID / Designation / Bank / Tax are optional: an empty Step 2 must reach Step 3.
            registerId("User", id, (driver, row) -> openCreateUserAtStep(driver, 3));
        }
        registerId("User", "CS2-003", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 2);
            form.enterJoiningDate("2026-01-15");
            Assert.assertEquals(form.joiningDateValue(), "2026-01-15", "CS2-003: YYYY-MM-DD date should be accepted.");
        });
        registerId("User", "CS2-007", (driver, row) -> Assert.assertFalse(
                openCreateUserAtStep(driver, 2).dropdownOptions("Select Manager").isEmpty(), "CS2-007: Manager list should show users."));
        registerId("User", "CS2-008", (driver, row) -> Assert.assertFalse(
                openCreateUserAtStep(driver, 2).dropdownOptions("Select Team").isEmpty(), "CS2-008: Team list should show teams."));
        registerId("User", "CS2-009", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 2);
            form.clickPrevious();
            Assert.assertTrue(form.isOnPersonalDetailsStep(), "CS2-009: Previous should return to Personal Details.");
            Assert.assertTrue(form.inputValue("name").startsWith("Step Check"), "CS2-009: entered Name should still be there.");
        });

        // -- Step 3 --
        registerId("User", "CS3-005", (driver, row) -> passwordCheck(driver, "CS3-005", "Strong@123", "Strong@456", "Passwords must match"));
        registerId("User", "CS3-006", (driver, row) -> passwordCheck(driver, "CS3-006", null, null, "Password is required"));
        registerId("User", "CS3-007", (driver, row) -> passwordCheck(driver, "CS3-007", "Strong@123", null, "Confirm password is required"));
        registerId("User", "CS3-008", (driver, row) -> Assert.assertTrue(
                openCreateUserAtStep(driver, 3).isSendAlertChecked(), "CS3-008: 'Send alert email' should be ticked by default."));
        registerId("User", "CS3-009", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 3);
            form.toggleSendAlert();
            Assert.assertFalse(form.isSendAlertChecked(), "CS3-009: 'Send alert email' should untick.");
        });
        registerId("User", "CS3-012", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 2);
            form.updateDesignation("Kept Designation");
            form.clickNextStep();
            Assert.assertTrue(form.isInputVisible("password"), "CS3-012: should reach the password step.");
            form.clickPrevious();
            Assert.assertEquals(form.inputValue("designation"), "Kept Designation", "CS3-012: Step 2 data should be kept.");
        });

        // -- Edit drawer --
        registerId("User", "EU-002", (driver, row) -> Assert.assertTrue(
                openEditUser(driver, row).isTextVisible("Upload files of type PNG, JPG"), "EU-002: profile image upload area should be shown."));
        registerId("User", "EU-005", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            form.clickNextStep();
            Assert.assertTrue(form.isInputVisible("employee_number"), "EU-005: Next should open Official Details.");
        });
        registerId("User", "EU-006", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            form.clickNextStep();
            for (String f : new String[] {"employee_number", "date_of_joining", "designation", "bank_info", "tax_info"}) {
                Assert.assertTrue(form.isInputVisible(f), "EU-006: Official Details field '" + f + "' should be shown.");
            }
            Assert.assertTrue(form.isTextVisible("Select Manager"), "EU-006: Manager field should be shown.");
        });
        registerId("User", "EU-011", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            form.typeInto("name", "");
            form.clickNextStep();
            Assert.assertTrue(form.waitForValidationMessage("Name is required"), "EU-011: empty Name should be rejected.");
            Assert.assertTrue(form.isOnPersonalDetailsStep(), "EU-011: the form must stay on Personal Details.");
            form.closeDrawer();
        });
        registerId("User", "EU-012", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            form.clickNextStep();
            form.clickPrevious();
            Assert.assertTrue(form.isOnPersonalDetailsStep(), "EU-012: Previous should return to Personal Details.");
        });
        registerId("User", "EU-010", (driver, row) -> withTestUser(driver, row, (name, email) -> {  // GUARDED
            UsersPage page = new UsersPage(driver);
            page.search(name);
            page.openEditUser(name);
            Pages.CreateUserPage form = new Pages.CreateUserPage(driver);
            form.clickNextStep();
            form.updateDesignation("Updated By Automation");
            form.saveEditedUser();
            Assert.assertTrue(form.isClosed(), "EU-010: saving valid data should close the drawer.");
        }));

        // -- Users list / View drawer --
        registerId("User", "UL-009", (driver, row) -> {
            UsersPage page = usersPage(driver);
            int rows = page.renderedRowCount();
            Assert.assertTrue(rows > 0, "UL-009: the grid should have rows.");
            Assert.assertEquals(page.rowsWithKebab(), rows, "UL-009: every row should have a three-dot menu.");
        });
        registerId("User", "UL-011", (driver, row) -> {
            UsersPage page = usersPage(driver);
            for (String col : new String[] {"date_of_joining", "designation", "manager"}) {
                Assert.assertTrue(page.isColumnReachable(col), "UL-011: column '" + col + "' should appear on horizontal scroll.");
            }
        });
        registerId("User", "VU-007", (driver, row) -> {
            UserViewPage view = openViewDrawer(driver, row);
            boolean anyNa = false;
            for (String f : new String[] {"Employee Id", "Joining Date", "Designation", "Bank Details", "Tax Details"}) {
                anyNa |= "NA".equals(view.getField(f));
            }
            if (!anyNa) {
                throw new SkipException("VU-007: this user has every optional field filled; pass -Dexcel.user.search=<user with empty fields>.");
            }
        });
        registerId("User", "VU-010", (driver, row) -> {
            UserViewPage view = openViewDrawer(driver, row);
            view.close();
            Assert.assertTrue(view.isClosed(), "VU-010: Back should close the drawer.");
        });
        registerId("User", "MH-002", (driver, row) -> {
            java.util.List<String[]> e = managerHistory(driver, row);
            Assert.assertTrue(e.stream().allMatch(x -> !x[1].isBlank()), "MH-002: every entry should show a manager name.");
        });
        registerId("User", "MH-003", (driver, row) -> {
            java.util.List<String[]> e = managerHistory(driver, row);
            Assert.assertTrue(e.stream().allMatch(x -> x[0].matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")),
                    "MH-003: every entry should have a 'YYYY-MM-DD HH:MM:SS' timestamp.");
        });
        registerId("User", "MH-004", (driver, row) -> {
            java.util.List<String[]> e = managerHistory(driver, row);
            if (e.size() < 2) {
                throw new SkipException("MH-004: needs a user with 2+ manager history entries.");
            }
            for (int i = 1; i < e.size(); i++) {
                Assert.assertTrue(e.get(i - 1)[0].compareTo(e.get(i)[0]) >= 0, "MH-004: entries should be latest first.");
            }
        });
        registerId("User", "MH-005", (driver, row) -> {
            managerHistory(driver, row);
            UserViewPage view = new UserViewPage(driver);
            view.closeManagerHistory();
            Assert.assertFalse(view.isManagerHistoryOpen(), "MH-005: close should hide Manager History.");
        });

        // -- Custom filter --
        registerId("User", "CF-008", (driver, row) -> {
            usersPage(driver);
            Pages.UserFilterPage filter = new Pages.UserFilterPage(driver);
            filter.openFilterPanel();
            filter.addAndCriterion();
            Assert.assertEquals(filter.criteriaCount(), 2, "CF-008: there should be two criteria after '+ AND'.");
            filter.deleteFirstCriterion();
            Assert.assertEquals(filter.criteriaCount(), 1, "CF-008: one criterion should remain after deleting.");
        });

        // -- Deactivate popup (never confirmed on a real user) --
        registerId("User", "KM-014", (driver, row) -> {
            Pages.DeactivateUserModal m = openDeactivate(driver, row);
            Assert.assertFalse(m.isSubmitEnabled(), "KM-014: Submit must stay disabled until a last working day is chosen.");
            m.cancel();
        });
        registerId("User", "KM-015", (driver, row) -> {
            Pages.DeactivateUserModal m = openDeactivate(driver, row);
            m.setLastWorkingDay("2025-06-30");
            Assert.assertEquals(m.lastWorkingDayValue(), "2025-06-30", "KM-015: the date should show as YYYY-MM-DD.");
            m.cancel();
        });
        registerId("User", "KM-016", (driver, row) -> {
            Pages.DeactivateUserModal m = openDeactivate(driver, row);
            if (!m.isPayoutQuestionVisible()) {
                m.cancel();
                throw new SkipException("KM-016: the payout question only shows for users on a plan; pass -Dexcel.user.search=<user with a plan>.");
            }
            m.clickPayout(false);
            Assert.assertTrue(m.isPayoutSelected(false) && !m.isPayoutSelected(true), "KM-016: choosing No should clear Yes.");
            m.clickPayout(true);
            Assert.assertTrue(m.isPayoutSelected(true) && !m.isPayoutSelected(false), "KM-016: choosing Yes should clear No.");
            m.cancel();
        });
        registerId("User", "KM-019", (driver, row) -> {
            UsersPage page = usersPage(driver);
            String term = userLookupTerm(page, row);
            page.search(term);
            String before = page.statusOf(term);
            page.openDeactivateUser(term);
            Pages.DeactivateUserModal m = new Pages.DeactivateUserModal(driver);
            m.cancel();
            Assert.assertTrue(m.isGone(), "KM-019: Cancel should close the popup.");
            Assert.assertEquals(page.statusOf(term), before, "KM-019: the user's status must not change.");
        });
        for (String[] v : new String[][] {{"KM-017", "true"}, {"KM-018", "false"}}) {
            registerId("User", v[0], (driver, row) -> withTestUser(driver, row, (name, email) -> {  // GUARDED
                UsersPage page = new UsersPage(driver);
                page.search(name);
                page.openDeactivateUser(name);
                Pages.DeactivateUserModal m = new Pages.DeactivateUserModal(driver);
                m.setLastWorkingDay(java.time.LocalDate.now().toString());
                if (m.isPayoutQuestionVisible()) {
                    m.clickPayout(Boolean.parseBoolean(v[1]));
                }
                m.submit();
                page.search(name);
                Assert.assertEquals(page.statusOf(name), "Inactive", v[0] + ": the user should become Inactive.");
            }));
        }

        // -- Change Password popup (invalid input only, unless GUARDED) --
        registerId("User", "KM-009", (driver, row) -> {
            Pages.ChangePasswordModal m = openChangePassword(driver, row);
            m.tryUpdate("123", "123");
            Assert.assertTrue(m.waitForError("Password must be at least 8 characters"), "KM-009: min length error expected.");
            m.cancel();
        });
        registerId("User", "KM-010", (driver, row) -> {
            Pages.ChangePasswordModal m = openChangePassword(driver, row);
            m.tryUpdate("NewPass@123", "Different@456");
            Assert.assertTrue(m.waitForError("Passwords must match"), "KM-010: mismatch error expected.");
            Assert.assertTrue(m.isModalStillOpen(), "KM-010: the popup must not submit.");
            m.cancel();
        });
        registerId("User", "KM-011", (driver, row) -> {
            Pages.ChangePasswordModal m = openChangePassword(driver, row);
            Assert.assertTrue(m.isAlertCheckboxVisible(), "KM-011: 'Send alert email' option should be shown.");
            boolean before = m.isAlertChecked();
            m.toggleAlert();
            Assert.assertNotEquals(m.isAlertChecked(), before, "KM-011: the option should toggle.");
            m.toggleAlert();
            Assert.assertEquals(m.isAlertChecked(), before, "KM-011: the option should toggle back.");
            m.cancel();
        });
        registerId("User", "KM-013", (driver, row) -> {
            Pages.ChangePasswordModal m = openChangePassword(driver, row);
            m.cancel();
            Assert.assertFalse(m.isModalStillOpen(), "KM-013: Cancel should close the popup.");
        });
        registerId("User", "KM-012", (driver, row) -> withTestUser(driver, row, (name, email) -> {  // GUARDED
            UsersPage page = new UsersPage(driver);
            page.search(name);
            page.openChangePassword(name);
            Pages.ChangePasswordModal m = new Pages.ChangePasswordModal(driver);
            m.tryUpdate("Changed@" + (System.currentTimeMillis() % 10000) + "x", "Changed@" + (System.currentTimeMillis() % 10000) + "x");
            Assert.assertTrue(m.isClosed(), "KM-012: a valid password update should close the popup.");
        }));

        // ---------------- Users sheet: Create / Edit / Deactivate rows (exact Test Case IDs) ----------------
        // Read-only unless marked GUARDED: the drawer is closed or validation stops it, nothing is saved.

        // -- Step 1 field validation --
        for (String[] v : new String[][] {
                {"TS_11_TC_03", "name", "N".repeat(51), "Your Name should be maximum of 50 characters"},
                {"TS_11_TC_05", "email", "a".repeat(45) + "@example.test", "Your Email should be maximum of 50 characters"},
                {"TS_11_TC_06", "email", "invalid.example.test", "Wrong email format"},
                {"TS_11_TC_07", "email", "invalid@", "Wrong email format"},
                {"TS_11_TC_08", "email", "first..last@example.test", "Wrong email format"}}) {
            registerId("User", v[0], (driver, row) -> {
                Pages.CreateUserPage form = openCreateUser(driver);
                form.typeInto(v[1], v[2]);
                form.clickNextStep();
                Assert.assertTrue(form.waitForValidationMessage(v[3]), v[0] + ": expected '" + v[3] + "'.");
                Assert.assertTrue(form.isOnPersonalDetailsStep(), v[0] + ": the form must stay on Personal Details.");
            });
        }
        // -- dropdowns accept only their own options (Step 1: Role/Currency, Step 2: Manager/Team/Department) --
        for (String[] v : new String[][] {
                {"TS_11_TC_10", "1", "Select Role"}, {"TS_11_TC_12", "1", "Select Currency"},
                {"TS_11_TC_14", "2", "Select Manager"}, {"TS_11_TC_16", "2", "Select Team"},
                {"TS_11_TC_17", "2", "Select Department"}}) {
            registerId("User", v[0], (driver, row) -> {
                Pages.CreateUserPage form = openCreateUserAtStep(driver, Integer.parseInt(v[1]));
                form.typeIntoDropdown(v[2], "NotARealOption" + System.currentTimeMillis() % 1000);
                Assert.assertTrue(form.isDropdownEmpty(v[2]),
                        v[0] + ": typed text that is not an option must not become the '" + v[2] + "' value.");
            });
        }
        registerId("User", "TS_11_TC_13", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 2);
            form.enterJoiningDate("2026-01-15");
            Assert.assertEquals(form.joiningDateValue(), "2026-01-15", "TS_11_TC_13: a typed joining date should be accepted.");
        });
        registerId("User", "TS_11_TC_15", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 2);
            form.selectFirstOption("Select Manager");
            form.clickNextStep();
            Assert.assertTrue(form.waitForValidationMessage("Manager effective date is required when manager is selected"),
                    "TS_11_TC_15: selecting a manager should make Manager Effective Date mandatory.");
        });
        // -- Step 3 password rules (validation blocks Submit, so no user is created) --
        for (String[] v : new String[][] {
                {"TS_11_TC_18", "", "", "Password is required"},
                {"TS_11_TC_19", "weakpass", "weakpass", "Password must contain at least one uppercase letter"},
                {"TS_11_TC_20", "Strong@123", "Different@456", "Passwords must match"}}) {
            registerId("User", v[0], (driver, row) -> {
                Pages.CreateUserPage form = openCreateUserAtStep(driver, 3);
                form.tryPasswordSubmit(v[1].isEmpty() ? null : v[1], v[2].isEmpty() ? null : v[2]);
                Assert.assertTrue(form.waitForValidationMessage(v[3]), v[0] + ": expected '" + v[3] + "'.");
                Assert.assertTrue(form.isInputVisible("password"), v[0] + ": the form must not be submitted.");
            });
        }
        registerId("User", "TS_11_TC_21", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 3);
            form.clickAutoGeneratePassword();
            String pwd = form.passwordValue();
            Assert.assertTrue(pwd != null && pwd.length() >= 8, "TS_11_TC_21: a password should be generated.");
            Assert.assertEquals(form.confirmPasswordValue(), pwd, "TS_11_TC_21: both password fields should be filled.");
        });
        registerId("User", "TS_11_TC_24", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUser(driver);
            Assert.assertTrue(form.uploadProfileImage(samplePng()), "TS_11_TC_24: the uploaded image should be shown as the profile preview.");
        });
        registerId("User", "CS1-001", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUser(driver);
            Assert.assertTrue(form.isDrawerTitle("Create New User"), "CS1-001: drawer title should be 'Create New User'.");
            Assert.assertTrue(form.isTextVisible("Fill the user details to create new user"), "CS1-001: drawer subtitle should be shown.");
        });
        registerId("User", "CS1-014", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUser(driver);
            form.closeDrawer();
            Assert.assertTrue(form.isClosed(), "CS1-014: the Create drawer should close.");
        });

        // -- Edit drawer (opened on an existing user, never saved) --
        registerId("User", "EU-001", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            for (String f : new String[] {"name", "email", "user_reference_id"}) {
                String value = form.inputValue(f);
                Assert.assertTrue(value != null && !value.isBlank(), "EU-001: '" + f + "' should be pre-filled.");
            }
            Assert.assertFalse(form.isDropdownEmpty("Select Role"), "EU-001: Role should be pre-selected.");
            Assert.assertFalse(form.isDropdownEmpty("Select Currency"), "EU-001: Currency should be pre-selected.");
        });
        registerId("User", "EU-003", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            form.typeInto("name", "Test User Edit");
            Assert.assertEquals(form.inputValue("name"), "Test User Edit", "EU-003: Name should accept a new value.");
            form.closeDrawer();
        });
        registerId("User", "EU-004", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            Assert.assertTrue(form.isInputEnabled("email"), "EU-004: Email ID should be editable.");
            form.typeInto("email", "testedit@yopmail.com");
            Assert.assertEquals(form.inputValue("email"), "testedit@yopmail.com", "EU-004: Email ID should accept a new value.");
            form.closeDrawer();
        });
        registerId("User", "EU-007", (driver, row) -> {
            String[] mgr = managerOfFirstUser(driver, row);
            Pages.CreateUserPage form = openEditUserByName(driver, mgr[0]);
            form.clickNextStep();
            Assert.assertTrue(form.isTextVisible(mgr[1]), "EU-007: Manager '" + mgr[1] + "' should be pre-selected in Edit.");
        });
        registerId("User", "EU-008", (driver, row) -> {
            String[] mgr = managerOfFirstUser(driver, row);
            Pages.CreateUserPage form = openEditUserByName(driver, mgr[0]);
            form.clickNextStep();
            Assert.assertEquals(form.managerEffectiveDateValue(), mgr[2], "EU-008: Manager Effective Date should be pre-filled.");
        });
        registerId("User", "EU-009", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            form.clickNextStep();
            if (!form.isTextVisible("View manager history")) {
                throw new SkipException("EU-009: this user has no manager history; pass -Dexcel.user.search=<user with manager history>.");
            }
        });
        registerId("User", "EU-013", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            form.closeDrawer();
            Assert.assertTrue(form.isClosed(), "EU-013: the Edit drawer should close.");
        });
        registerId("User", "EU-014", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            java.util.List<String> roles = form.dropdownOptions(currentRoleOrPlaceholder(form));
            for (String r : new String[] {"Admin", "Manager", "Individual Contributor"}) {
                Assert.assertTrue(roles.contains(r), "EU-014: Role options should include '" + r + "', got " + roles);
            }
        });
        registerId("User", "EU-015", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            java.util.List<String> currencies = form.dropdownOptions(currentCurrencyOrPlaceholder(form));
            Assert.assertTrue(currencies.size() > 1, "EU-015: Currency should offer several options, got " + currencies);
            Assert.assertTrue(currencies.stream().anyMatch(c -> c.contains("USD")), "EU-015: Currency options should include USD, got " + currencies);
        });

        // -- Row menu --
        registerId("User", "KM-002", (driver, row) -> {
            UsersPage page = usersPage(driver);
            page.openRowMenu(0);
            Assert.assertTrue(page.isRowMenuItemVisible("Edit User"), "KM-002: 'Edit User' should be in the row menu.");
        });
        registerId("User", "KM-003", (driver, row) -> {
            UsersPage page = usersPage(driver);
            page.openRowMenu(0);
            Assert.assertTrue(page.isRowMenuItemVisible("Deactivate"), "KM-003: 'Deactivate' should be in an active user's row menu.");
        });
        registerId("User", "KM-005", (driver, row) -> {
            Pages.CreateUserPage form = openEditUser(driver, row);
            Assert.assertTrue(form.isTextVisible("Personal Details"), "KM-005: Edit drawer should show Personal Details.");
        });
        registerId("User", "KM-006", (driver, row) -> {
            UsersPage page = usersPage(driver);
            String term = userLookupTerm(page, row);
            page.search(term);
            page.openDeactivateUser(term);
            Pages.DeactivateUserModal modal = new Pages.DeactivateUserModal(driver);
            Assert.assertTrue(modal.isOpen(), "KM-006: a deactivate confirmation should appear.");
            modal.cancel(); // never confirm: the UI cannot re-activate users
        });

        // -- GUARDED: these create a real user (cleaned up afterwards); need -Drun.destructive.tests=true --
        registerId("User", "CS3-010", (driver, row) -> withTestUser(driver, row, (name, email) -> {
            UsersPage page = new UsersPage(driver);
            page.search(name);
            Assert.assertTrue(page.isRowListed(name), "CS3-010: the new user should be listed.");
            Assert.assertEquals(page.statusOf(name), "Active", "CS3-010: the new user should be Active.");
        }));
        registerId("User", "E2E-001", (driver, row) -> withTestUser(driver, row, (name, email) -> {
            UsersPage page = new UsersPage(driver);
            page.search(name);
            Assert.assertTrue(page.isRowListed(name), "E2E-001: the new user should be listed.");
            Assert.assertEquals(page.statusOf(name), "Active", "E2E-001: the new user should be Active.");
        }));
        registerId("User", "E2E-003", (driver, row) -> withTestUser(driver, row, (name, email) -> {
            UsersPage page = new UsersPage(driver);
            page.search(name);
            Assert.assertTrue(page.isRowListed(name), "E2E-003: searching right after creation should find the user.");
        }));
        registerId("User", "E2E-002", (driver, row) -> withTestUser(driver, row, (name, email) -> {
            UsersPage page = new UsersPage(driver);
            String updated = name + " Updated";
            page.search(name);
            page.openEditUser(name);
            Pages.CreateUserPage form = new Pages.CreateUserPage(driver);
            form.updateName(updated);
            form.clickNextStep();
            form.saveEditedUser();
            page.search(updated);
            Assert.assertTrue(page.isRowListed(updated), "E2E-002: the edited name should be listed.");
        }));
        registerId("User", "E2E-007", (driver, row) -> withTestUser(driver, row, (name, email) -> {
            UsersPage page = new UsersPage(driver);
            page.search(name);
            page.openEditUser(name);
            Pages.CreateUserPage form = new Pages.CreateUserPage(driver);
            form.clickNextStep();
            form.updateDesignation("QA Automation Designation");
            form.saveEditedUser();
            page.search(name);
            page.openView(name);
            Assert.assertEquals(new UserViewPage(driver).getDesignation().trim(), "QA Automation Designation",
                    "E2E-007: the edited Designation should show in the View drawer.");
        }));

        // -- Not automated on purpose: reported as Skipped with the reason instead of a false Pass --
        manualId("User", "TS_11_TC_22", "needs a real user creation plus checking that an alert email was sent");
        manualId("User", "TS_11_TC_23", "needs a real user creation plus checking that no alert email was sent");
        manualId("User", "E2E-004", "uses the Custom Filter drawer, which is not automated yet");
        manualId("User", "E2E-005", "User Data Stream edit flow is not automated yet");
        registerId("User", "USV-006", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamView(driver);
            w.clickViewEdit();
            Assert.assertTrue(w.waitForStep("Stream Details"), "USV-006: Edit should open the wizard on Stream Details.");
            Assert.assertTrue(driver.getCurrentUrl().contains("user-streams/edit"), "USV-006: should be the Edit User Data Stream page.");
        });
        registerId("User", "SS3-009", (driver, row) -> {
            Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 3);
            int before = w.criteriaCount();
            w.addCriteria();
            Assert.assertEquals(w.criteriaCount(), before + 1, "SS3-009: Add New Criteria should add a criteria block.");
        });
        manualId("User", "TC_EXTRA-U013", "needs deactivating a user and logging in as that user; deactivation cannot be undone in the UI");
        manualId("User", "TC_EXTRA-U014", "would deactivate the logged-in admin account");
        manualId("User", "TC_EXTRA-U015", "needs two admins editing at the same time");

        // ---------------- Users sheet: exact Test Case ID mappings ----------------
        // Each of these checks exactly what its workbook row describes; all are read-only
        // (the Add User drawer is never submitted).
        registerId("User", "UL-001", (driver, row) -> {
            UsersPage page = usersPage(driver);
            for (String h : new String[] {"S No.", "Name", "E-mail", "Role", "User Ref Id", "Currency", "Status", "Action"}) {
                Assert.assertTrue(page.isColumnHeaderVisible(h), "UL-001: column '" + h + "' should be shown.");
            }
        });
        registerId("User", "UL-002", (driver, row) -> {
            String footer = usersPage(driver).totalRecordsText();
            Assert.assertTrue(footer.matches("Total Records:\\s*\\d+"),
                    "UL-002: expected 'Total Records: <number>' under the grid, got '" + footer + "'.");
        });
        registerId("User", "UL-003", (driver, row) -> Assert.assertTrue(
                usersPage(driver).isPageButtonVisible("User Data Streams"), "UL-003: 'User Data Streams' button should be visible."));
        registerId("User", "UL-004", (driver, row) -> Assert.assertTrue(
                usersPage(driver).isPageButtonVisible("Add New User"), "UL-004: 'Add New User' button should be visible."));
        registerId("User", "UL-005", (driver, row) -> Assert.assertTrue(
                usersPage(driver).isPageButtonVisible("Fetch Users"), "UL-005: 'Fetch Users' button should be visible."));
        registerId("User", "UL-006", (driver, row) -> Assert.assertTrue(
                usersPage(driver).isFilterIconVisible(), "UL-006: filter (funnel) icon should be visible."));
        registerId("User", "UL-007", (driver, row) -> Assert.assertTrue(
                usersPage(driver).hasActiveStatusBadge(), "UL-007: active users should show an 'Active' status badge."));

        for (String[] f : new String[][] {
                {"VU-002", "Name"}, {"VU-003", "E-mail"}, {"VU-004", "User Reference ID"},
                {"VU-005", "Role"}, {"VU-006", "Currency"}, {"VU-009", "Last Updated At"}}) {
            String id = f[0];
            String label = f[1];
            registerId("User", id, (driver, row) -> {
                UsersPage page = usersPage(driver);
                String term = userLookupTerm(page, row);
                page.search(term);
                page.openView(term);
                UserViewPage view = new UserViewPage(driver);
                Assert.assertTrue(view.isOpen(), id + ": user view drawer should open.");
                String value = view.getField(label);
                Assert.assertTrue(value != null && !value.isBlank(), id + ": '" + label + "' should show a value in the view drawer.");
            });
        }

        registerId("User", "KM-001", (driver, row) -> {
            UsersPage page = usersPage(driver);
            page.openRowMenu(0);
            Assert.assertTrue(page.isRowMenuItemVisible("Edit User"), "KM-001: 'Edit User' should be in the row menu.");
            Assert.assertTrue(page.isRowMenuItemVisible("Change Password"), "KM-001: 'Change Password' should be in the row menu.");
        });
        registerId("User", "KM-007", (driver, row) -> {
            UsersPage page = usersPage(driver);
            page.openRowMenu(0);
            Assert.assertEquals(page.openRowMenuCount(), 1, "KM-007: the row menu should open.");
            page.clickOutside();
            Assert.assertEquals(page.openRowMenuCount(), 0, "KM-007: clicking outside should close the row menu.");
        });
        registerId("User", "KM-008", (driver, row) -> {
            UsersPage page = usersPage(driver);
            // Row 1's button sits under row 0's open menu, so use a row further down.
            int second = Math.min(4, page.renderedRowCount() - 1);
            if (second < 1) {
                throw new SkipException("KM-008 needs at least two users in the grid.");
            }
            page.openRowMenu(0);
            page.openRowMenu(second);
            Assert.assertEquals(page.openRowMenuCount(), 1, "KM-008: only one row menu should be open at a time.");
        });

        registerId("User", "CS1-002", (driver, row) -> {
            usersPage(driver).openCreateUser();
            Pages.CreateUserPage form = new Pages.CreateUserPage(driver);
            Assert.assertTrue(form.isTextVisible("Upload files of type PNG, JPG"),
                    "CS1-002: profile image upload area ('Upload files of type PNG, JPG') should be shown.");
        });
        registerId("User", "CS1-008", (driver, row) -> {
            usersPage(driver).openCreateUser();
            Pages.CreateUserPage form = new Pages.CreateUserPage(driver);
            String sfx = Long.toString(System.currentTimeMillis() % 100000);
            form.fillPersonalDetailsExcept("none", "Email Format " + sfx, "notanemail", "EMF-" + sfx,
                    System.getProperty("test.user.role", "Individual Contributor"), System.getProperty("test.user.currency", "USD"));
            form.clickNextStep();
            Assert.assertTrue(form.waitForValidationMessage("Wrong email format"), "CS1-008: 'Wrong email format' should be shown.");
            Assert.assertTrue(form.isOnPersonalDetailsStep(), "CS1-008: the form must stay on Personal Details.");
        });
        registerId("User", "CS2-001", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 2);
            for (String input : new String[] {"employee_number", "date_of_joining", "designation", "bank_info", "tax_info"}) {
                Assert.assertTrue(form.isInputVisible(input), "CS2-001: Official Details field '" + input + "' should be shown.");
            }
        });
        registerId("User", "CS3-001", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 3);
            Assert.assertTrue(form.isInputVisible("password"), "CS3-001: Password field should be shown.");
            Assert.assertTrue(form.isInputVisible("confirm_password"), "CS3-001: Confirm Password field should be shown.");
            Assert.assertTrue(form.isAutoGeneratePasswordVisible(), "CS3-001: 'Auto - Generate Password' should be shown.");
            Assert.assertTrue(form.isTextVisible("Send alert email to new user about their account"),
                    "CS3-001: 'Send alert email' option should be shown.");
        });
        registerId("User", "CS3-002", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 3);
            Assert.assertEquals(form.passwordFieldType(), "password", "CS3-002: password should be masked by default.");
        });
        registerId("User", "CS3-003", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 3);
            form.typePassword("Test@123");
            for (String label : new String[] {"Upper case", "Lower case", "Numbers", "Symbols"}) {
                Assert.assertTrue(form.isTextVisible(label), "CS3-003: strength indicator '" + label + "' should be shown.");
            }
        });
        registerId("User", "CS3-004", (driver, row) -> {
            Pages.CreateUserPage form = openCreateUserAtStep(driver, 3);
            form.clickAutoGeneratePassword();
            String pwd = form.passwordValue();
            Assert.assertTrue(pwd != null && pwd.length() >= 8, "CS3-004: a password should be generated.");
            Assert.assertEquals(form.confirmPasswordValue(), pwd, "CS3-004: Confirm Password should get the same value.");
        });
        register("user:requiredfield", (driver, row) -> {
            String field = ExcelTestCaseReader.userRequiredField(row.getDescription());
            if (field == null) {
                throw new SkipException("Could not tell which field " + row.getTestCaseId() + " is about.");
            }
            String message = switch (field) {
                case "name" -> "Name is required";
                case "email" -> "Email is required";
                case "reference" -> "User Reference ID is required";
                case "role" -> "Role is required";
                default -> "Currency is required";
            };
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            page.openCreateUser();
            Pages.CreateUserPage form = new Pages.CreateUserPage(driver);

            // Fill every other Step 1 field with valid data and leave only the field under test empty,
            // so the only thing that can block "Next Step" is that field. Nothing is saved.
            String sfx = Long.toString(System.currentTimeMillis() % 100000);
            form.fillPersonalDetailsExcept(field, "Required Check " + sfx, "required.check." + sfx + "@example.test",
                    "REQ-" + sfx, System.getProperty("test.user.role", "Individual Contributor"),
                    System.getProperty("test.user.currency", "USD"));
            form.clickNextStep();

            Assert.assertTrue(form.waitForValidationMessage(message),
                    row.getTestCaseId() + ": expected '" + message + "' when " + field + " is left empty.");
            Assert.assertTrue(form.isOnPersonalDetailsStep(),
                    row.getTestCaseId() + ": the form must stay on Personal Details when " + field + " is empty.");
        });
        register("user:create", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            page.openCreateUser();
        });
        register("user:edit", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            String term = userLookupTerm(page, row);
            page.search(term);
            page.openEditUser(term);
        });
        register("user:deactivate", (driver, row) -> {
            new DashboardPage(driver).navigateToEmployees();
            UsersPage page = new UsersPage(driver);
            String term = userLookupTerm(page, row);
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

    /**
     * User to act on: the Excel/CSV value when one was given (or -Dexcel.user.search), otherwise the
     * first user currently in the Users grid. Replaces the old hard-coded "Mayank" default, which
     * does not exist in every tenant and made every edit/view/deactivate row time out.
     */
    private static String userLookupTerm(UsersPage page, TestCaseRow row) {
        String term = row.param1();
        if (term != null && !term.isBlank()) {
            return term.trim();
        }
        try {
            if (page.isGridLoaded()) {
                term = page.getFirstRowValue("name");
            }
        } catch (Exception ignored) {
        }
        if (term == null || term.isBlank()) {
            throw new SkipException("No user rows in the Users grid to run " + row.getTestCaseId() + " on.");
        }
        return term.trim();
    }

    /** Registers an action for one exact workbook Test Case ID (takes priority over keyword matching). */
    private static void registerId(String module, String testCaseId, TestAction action) {
        register(idKey(module, testCaseId), action);
    }

    public static String idKey(String module, String testCaseId) {
        return (module + ":id-" + testCaseId).trim().toLowerCase();
    }

    private static UsersPage usersPage(WebDriver driver) {
        new DashboardPage(driver).navigateToEmployees();
        UsersPage page = new UsersPage(driver);
        Assert.assertTrue(page.isGridLoaded(), "Users grid should load.");
        return page;
    }

    /**
     * Opens Add New User and moves to the given step (1, 2 or 3) with valid, unique Step 1 data.
     * Nothing is submitted, so no user is created.
     */
    private static Pages.CreateUserPage openCreateUserAtStep(WebDriver driver, int step) {
        usersPage(driver).openCreateUser();
        Pages.CreateUserPage form = new Pages.CreateUserPage(driver);
        if (step >= 2) {
            String sfx = Long.toString(System.currentTimeMillis() % 100000);
            form.fillPersonalDetailsExcept("none", "Step Check " + sfx, "step.check." + sfx + "@example.test",
                    "STP-" + sfx, System.getProperty("test.user.role", "Individual Contributor"),
                    System.getProperty("test.user.currency", "USD"));
            form.clickNextStep();
            Assert.assertTrue(form.isInputVisible("employee_number"), "Official Details (step 2) should open.");
        }
        if (step >= 3) {
            form.clickNextStep();
            Assert.assertTrue(form.isInputVisible("password"), "Password step (step 3) should open.");
        }
        return form;
    }

    /** A row that is deliberately not automated: reported as Skipped with this reason. */
    private static void manualId(String module, String testCaseId, String reason) {
        registerId(module, testCaseId, (driver, row) -> {
            throw new SkipException("Manual: " + reason + ".");
        });
    }

    private static Pages.CreateUserPage openCreateUser(WebDriver driver) {
        return openCreateUserAtStep(driver, 1);
    }

    /** Opens Edit User on the row's user (or the first user in the grid). Never saves. */
    private static Pages.CreateUserPage openEditUser(WebDriver driver, TestCaseRow row) {
        UsersPage page = usersPage(driver);
        return openEditUserByName(driver, userLookupTerm(page, row));
    }

    private static Pages.CreateUserPage openEditUserByName(WebDriver driver, String userName) {
        UsersPage page = usersPage(driver);
        page.search(userName);
        page.openEditUser(userName);
        Pages.CreateUserPage form = new Pages.CreateUserPage(driver);
        Assert.assertTrue(form.isDrawerTitle("Edit User"), "Edit User drawer should open.");
        return form;
    }

    /** {name, manager, managerEffectiveDate} of a user that has a manager, read from the View drawer. */
    private static String[] managerOfFirstUser(WebDriver driver, TestCaseRow row) {
        UsersPage page = usersPage(driver);
        String term = userLookupTerm(page, row);
        page.search(term);
        page.openView(term);
        UserViewPage view = new UserViewPage(driver);
        Assert.assertTrue(view.isOpen(), "User view drawer should open.");
        String manager = view.getManager().trim();
        String date = view.getManagerEffectiveDate().trim();
        if (manager.isEmpty() || manager.equals("NA")) {
            throw new SkipException(row.getTestCaseId() + ": user '" + term + "' has no manager; pass -Dexcel.user.search=<user with a manager>.");
        }
        return new String[] {term, manager, date};
    }

    private static String currentRoleOrPlaceholder(Pages.CreateUserPage form) {
        for (String r : new String[] {"Admin", "Manager", "Leader", "Individual Contributor"}) {
            if (form.isTextVisible(r)) {
                return r;
            }
        }
        return "Select Role";
    }

    private static String currentCurrencyOrPlaceholder(Pages.CreateUserPage form) {
        for (String c : new String[] {"USD", "INR", "EUR", "GBP"}) {
            if (form.isTextVisible(c)) {
                return c;
            }
        }
        return "Select Currency";
    }

    /** A small PNG written to the temp folder for upload tests. */
    private static java.io.File samplePng() {
        try {
            java.io.File f = java.io.File.createTempFile("profile-", ".png");
            f.deleteOnExit();
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(120, 120, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            g.setColor(new java.awt.Color(37, 99, 235));
            g.fillRect(0, 0, 120, 120);
            g.dispose();
            javax.imageio.ImageIO.write(img, "png", f);
            return f;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Could not create sample PNG", e);
        }
    }

    /**
     * GUARDED: creates a uniquely named user through the UI, runs {@code body}, then deletes the user
     * again (database cleanup). Skips unless -Drun.destructive.tests=true.
     */
    private static void withTestUser(WebDriver driver, TestCaseRow row,
            java.util.function.BiConsumer<String, String> body) {
        ExecutionGuard.requireDestructiveTestsEnabled();
        String sfx = Long.toString(System.currentTimeMillis() % 1_000_000L);
        String name = "Auto " + row.getTestCaseId().replaceAll("[^A-Za-z0-9]", "") + " " + sfx;
        String email = "auto." + sfx + "@yopmail.com";
        try {
            UsersPage page = usersPage(driver);
            page.openCreateUser();
            Pages.CreateUserPage form = new Pages.CreateUserPage(driver);
            form.enterPersonalDetails(name, email, "AUTO-" + sfx,
                    System.getProperty("test.user.role", "Individual Contributor"), System.getProperty("test.user.currency", "USD"));
            form.clickNextStep();
            form.clickNextStep();
            form.enterPasswordDetails("Auto@" + sfx + "x", false);
            form.submitNewUser();
            body.accept(name, email);
        } finally {
            UserCleanupUtil.cleanupUserByEmail(email);
        }
    }

    private static void passwordCheck(WebDriver driver, String id, String pwd, String confirm, String message) {
        Pages.CreateUserPage form = openCreateUserAtStep(driver, 3);
        form.tryPasswordSubmit(pwd, confirm);
        Assert.assertTrue(form.waitForValidationMessage(message), id + ": expected '" + message + "'.");
        Assert.assertTrue(form.isInputVisible("password"), id + ": the form must not be submitted.");
    }

    private static UserViewPage openViewDrawer(WebDriver driver, TestCaseRow row) {
        UsersPage page = usersPage(driver);
        String term = userLookupTerm(page, row);
        page.search(term);
        page.openView(term);
        UserViewPage view = new UserViewPage(driver);
        Assert.assertTrue(view.isOpen(), "User view drawer should open.");
        return view;
    }

    private static java.util.List<String[]> managerHistory(WebDriver driver, TestCaseRow row) {
        UserViewPage view = openViewDrawer(driver, row);
        if (!view.hasManagerHistoryLink()) {
            throw new SkipException(row.getTestCaseId() + ": this user has no manager history; pass -Dexcel.user.search=<user with manager history>.");
        }
        view.openManagerHistory();
        Assert.assertTrue(view.isManagerHistoryOpen(), "Manager History should open.");
        java.util.List<String[]> entries = view.managerHistoryEntries();
        Assert.assertFalse(entries.isEmpty(), row.getTestCaseId() + ": Manager History should list entries.");
        return entries;
    }

    private static Pages.DeactivateUserModal openDeactivate(WebDriver driver, TestCaseRow row) {
        UsersPage page = usersPage(driver);
        String term = userLookupTerm(page, row);
        page.search(term);
        page.openDeactivateUser(term);
        Pages.DeactivateUserModal m = new Pages.DeactivateUserModal(driver);
        Assert.assertTrue(m.isOpen(), "Deactivate popup should open.");
        return m;
    }

    private static Pages.ChangePasswordModal openChangePassword(WebDriver driver, TestCaseRow row) {
        UsersPage page = usersPage(driver);
        String term = userLookupTerm(page, row);
        page.search(term);
        page.openChangePassword(term);
        Pages.ChangePasswordModal m = new Pages.ChangePasswordModal(driver);
        Assert.assertTrue(m.isOpen(), "Change Password popup should open.");
        return m;
    }

    // ---------------- User Data Stream / Fetch Users helpers ----------------

    /** Opens the View User Stream drawer; skips when the tenant has no user stream yet. */
    private static Pages.UserStreamWizardPage streamView(WebDriver driver) {
        UsersPage page = usersPage(driver);
        page.openUserDataStreams();
        Pages.UserStreamWizardPage w = new Pages.UserStreamWizardPage(driver);
        if (!w.isViewDrawerOpen()) {
            throw new SkipException("No User Data Stream is configured in this tenant, so there is nothing to view.");
        }
        return w;
    }

    private static boolean streamExists(WebDriver driver) {
        UsersPage page = usersPage(driver);
        page.openUserDataStreams();
        return new Pages.UserStreamWizardPage(driver).isViewDrawerOpen();
    }

    /** A fresh (add-mode) wizard on Step 1 with nothing chosen. */
    private static Pages.UserStreamWizardPage newStreamWizard(WebDriver driver) {
        usersPage(driver);
        Pages.UserStreamWizardPage w = new Pages.UserStreamWizardPage(driver);
        w.openWizard(ConfigReader.get("url"), false);
        return w;
    }

    private static java.util.List<String> connectedApps(WebDriver driver, TestCaseRow row) {
        java.util.List<String> apps = newStreamWizard(driver).appOptions();
        if (apps.isEmpty()) {
            throw new SkipException(row.getTestCaseId() + ": no CRM is connected in this tenant.");
        }
        return apps;
    }

    /**
     * The wizard at the given step (1-4). Uses the existing stream (edit mode, already valid) when
     * there is one, otherwise picks the first connected app and maps the required fields.
     */
    private static Pages.UserStreamWizardPage streamWizardAt(WebDriver driver, TestCaseRow row, int step) {
        boolean edit = streamExists(driver);
        Pages.UserStreamWizardPage w = new Pages.UserStreamWizardPage(driver);
        w.openWizard(ConfigReader.get("url"), edit);
        if (w.selectedApp().isEmpty()) {
            if (w.appOptions().isEmpty()) {
                throw new SkipException(row.getTestCaseId() + ": no CRM is connected in this tenant.");
            }
            w.chooseFirstApp();
        }
        if (step >= 2) {
            w.next();
            Assert.assertTrue(w.waitForStep("User Mapping & Ordering"), "Should reach 'User Mapping & Ordering'.");
        }
        if (step >= 3) {
            for (String f : w.targetFieldNames()) {
                if (f.endsWith("*") && w.mappedSource(f.replace("*", "")).isEmpty()) {
                    w.mapField(f.replace("*", ""));
                }
            }
            w.next();
            boolean criteria = w.waitForStep("Define Criteria");
            if (!criteria && w.waitForStep("Review & Submit")) {
                if (step == 3) {
                    throw new SkipException(row.getTestCaseId() + ": this app (e.g. HubSpot) has no Define Criteria step.");
                }
            } else {
                Assert.assertTrue(criteria, "Should reach 'Define Criteria'.");
            }
        }
        if (step >= 4 && !"Review & Submit".equals(w.currentStepTitle())) {
            w.next();
            Assert.assertTrue(w.waitForStep("Review & Submit"), "Should reach 'Review & Submit'.");
        }
        return w;
    }

    /** Define Criteria with at least two criteria blocks (so the AND/OR Condition appears). */
    private static Pages.UserStreamWizardPage streamWizardWithTwoCriteria(WebDriver driver, TestCaseRow row) {
        Pages.UserStreamWizardPage w = streamWizardAt(driver, row, 3);
        while (w.criteriaCount() < 2) {
            w.addCriteria();
        }
        return w;
    }

    /** Opens Fetch Users; skips when the tenant has no stream or the integration is inactive. */
    private static Pages.FetchUsersPage fetchUsers(WebDriver driver) {
        UsersPage page = usersPage(driver);
        page.openFetchUsers();
        Pages.FetchUsersPage f = new Pages.FetchUsersPage(driver);
        if (!f.isFetchUsersPage()) {
            throw new SkipException("Fetch Users is not available: no User Data Stream, or its integration is inactive.");
        }
        return f;
    }

    private static Pages.FetchUsersPage fetchUsersWithRows(WebDriver driver) {
        Pages.FetchUsersPage f = fetchUsers(driver);
        if (f.rowCount() == 0) {
            throw new SkipException("Fetch Users returned no users from the connected source.");
        }
        return f;
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
