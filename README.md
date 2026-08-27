# Visdum Automation

Selenium + Java + TestNG UI automation for **Visdum** (visdum-v2-frontend), covering the
**Employees/Users** module and the **Teams** module. Built with Maven, ExtentReports for HTML
reporting, and WebDriverManager for zero-config driver setup.

---

## 1. Prerequisites

| Tool | Version | Check with |
|---|---|---|
| Java (JDK) | 17+ | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| Google Chrome | latest | already on your machine |

WebDriverManager auto-downloads the matching ChromeDriver binary on first run — you don't need to
install it manually. Your machine needs internet access the first time you run tests (both to
resolve Maven dependencies and to download the ChromeDriver binary).

---

## 2. One-time setup

### 2.1 Clone / unzip the project
Open the `VisdumAutomation` folder in your terminal (or IntelliJ/Eclipse).

### 2.2 Configure the target environment and login
Edit **`src/test/resources/config.properties`**:

```properties
url=https://uat.visdum.com
username=your.uat.user@example.com
password=YourPassword123
```

This account is used for the `@BeforeMethod` login step in every test (`Base/BaseTest.java` and
`Base/TeamsBaseTest.java`).

### 2.3 (Recommended) Resolve dependencies once
```
mvn -q dependency:resolve
```

---

## 3. Running the tests

### 3.1 Run everything (recommended)
Runs the **Excel-driven suite** defined in `testng-excel.xml` — it reads every sheet of the QA
workbook (`src/test/resources/manual-testcases.xlsx`), executes the auto-mappable read-only
scenarios (Search / Sort / ColumnFilter / View for User, Team, Department) and reports every other
row (Login module, destructive/complex scenarios) as Skipped with a reason. The ExtentReports
listener is attached automatically.
```
mvn test
```

> The **data-driven suite** (`testng-datadriven.xml`, `tests/datadriven.DataDrivenTest`) does **not**
run by default. To run it (or the original class-based `testng.xml`) instead, temporarily point
`pom.xml` at the desired suite:
 
```xml
<!-- pom.xml -->
<suiteXmlFiles>
    <suiteXmlFile>testng-datadriven.xml</suiteXmlFile>
</suiteXmlFiles>
```
 
Then `mvn test`. (The `suiteXmlFiles` value in `pom.xml` takes precedence, so a `-DsuiteXmlFile=...`
command-line flag is ignored while it's set.)
 
> ⚠️ `testng.xml` includes **destructive** tests (Create/Edit/Deactivate/status-change). By default they
> **skip themselves** unless you explicitly enable them — see §3.4. Safe read-only tests
> (Search, Sort, Filter, View, Validation) always run.

### 3.2 Run only one test class
```
mvn test -Dtest=SearchUserTest
```
```
mvn test -Dtest=ColumnFilterUserTest
```

> ⚠️ `-Dtest=...` is only honoured when `pom.xml` has **no** `<suiteXmlFiles>` block (the block takes
> precedence and makes Maven run the suite instead). To run a single class, either remove/comment the
> `suiteXmlFiles` block in `pom.xml`, or run the class directly from your IDE.

### 3.3 Run a single test method (including all `@DataProvider` rows)
```
mvn test "-Dtest=tests.employees.SortUserTest#sortingColumnTogglesAriaSort"
```

> **Copy/paste tip:** type or paste this as **one single line**. If your terminal wraps a long
> line and you copy from the wrapped display, a stray line-break can land mid-argument (e.g.
> splitting `-Dtest=tests.employees.SortUserTest` into two tokens), which Maven then
> misinterprets as an unknown lifecycle phase. If you ever see an error like
> `Unknown lifecycle phase ".employees.SomeTest#someMethod"`, this copy/paste split is almost
> always the cause — retype the command on one line instead of pasting it.

### 3.4 Run destructive tests (Create, Edit, Deactivate, status changes)
These are gated by `utilities/ExecutionGuard`, which skips them unless you pass
`-Drun.destructive.tests=true`. Some also require you to tell the test which existing record to
act on, since they don't invent test data to edit/deactivate.

```
# Create a brand-new user (safe — creates fresh data, no existing record needed)
mvn test -Dtest=CreateUserTest -Drun.destructive.tests=true

# Edit an existing user (must already exist in the target org)
mvn test -Dtest=EditUserTest -Drun.destructive.tests=true -Dtest.user.existing="Mayank"

# Change an existing user's password
mvn test -Dtest=ChangeUserPasswordTest -Drun.destructive.tests=true -Dtest.user.existing="Mayank"

# Deactivate an existing (currently Active) user
mvn test -Dtest=DeactivateUserTest -Drun.destructive.tests=true -Dtest.user.existing="Mayank"

# Create a brand-new team
mvn test -Dtest=CreateTeamTest -Drun.destructive.tests=true

# Edit an existing team
mvn test -Dtest=EditTeamTest -Drun.destructive.tests=true -Dtest.team.existing="Sales"

# Add an existing user to a team (search results must have at least one selectable/unassigned user)
mvn test -Dtest=AddTeamMembersTest -Drun.destructive.tests=true -Dtest.team.existing="Sales"

# Deactivate an existing team -- team must currently be Active
mvn test -Dtest=DeactivateTeamTest -Drun.destructive.tests=true -Dtest.team.existing="Sales"

# Activate an existing team -- team must currently be Inactive
mvn test -Dtest=ActivateTeamTest -Drun.destructive.tests=true -Dtest.team.existing="Sales"

# ---- Departments (mirror of Teams; use -Dtest.department.existing) ----
mvn test -Dtest=CreateDepartmentTest -Drun.destructive.tests=true
mvn test -Dtest=EditDepartmentTest -Drun.destructive.tests=true -Dtest.department.existing="Sales Engineers"
mvn test -Dtest=DuplicateDepartmentTest -Drun.destructive.tests=true -Dtest.department.existing="Sales Engineers"
mvn test -Dtest=AddDepartmentMembersTest -Drun.destructive.tests=true -Dtest.department.existing="Sales Engineers"
mvn test -Dtest=DeactivateDepartmentTest -Drun.destructive.tests=true -Dtest.department.existing="Sales Engineers"
mvn test -Dtest=ActivateDepartmentTest -Drun.destructive.tests=true -Dtest.department.existing="Sales Engineers"
```

Other optional overrides for `CreateUserTest` (all have sensible defaults if omitted):
```
-Dtest.user.password=Test@1234
-Dtest.user.role="Individual Contributor"
-Dtest.user.currency=INR
-Dtest.user.manager="Some Manager"
-Dtest.user.team="Some Team"
-Dtest.user.department="Some Department"
```

### 3.5 Run only the Employees suite or only the Teams suite
`testng.xml` groups tests into two `<test>` blocks ("Employees Test" and "Teams Test"). To run just
one block:
```
mvn test -DsuiteXmlFile=testng.xml -Dgroups=  # (not group-based; use -Dtest instead, see below)
```
In practice it's simpler to just list the classes you want:
```
mvn test -Dtest=SearchUserTest,UserValidationTest,SortUserTest,FilterUserTest,ColumnFilterUserTest,ViewUserTest,CreateUserTest,EditUserTest,ChangeUserPasswordTest,DeactivateUserTest
```
```
mvn test -Dtest=SearchTeamTest,TeamValidationTest,CreateTeamTest,EditTeamTest,ChangeTeamStatusTest
```

---

## 4. What each test covers

### Login module (`tests/login`)
Read-only login validation/error flows — no OTP required (the OTP/2FA completion itself stays manual).
| Test | Covers |
|---|---|
| `LoginValidationTest` | Empty fields, only-email, only-password, invalid email format, email > 50 chars, password < 8 chars (Yup validations) |
| `InvalidCredentialsTest` | Wrong password → "Invalid Credentials"; unregistered email → "selected email is invalid" |
| `ValidLoginTest` | Valid credentials redirect to the 2-step verification screen |

### Employees / Users module (`tests/employees`)
| Test | Covers |
|---|---|
| `SearchUserTest` | Search box |
| `UserValidationTest` | Required-field validation on create |
| `SortUserTest` | Column header sort (ascending/descending) — **all 17 sortable columns**, data-driven |
| `ColumnFilterUserTest` | AG-Grid's built-in per-column filter menu (the "☰" icon on hover) — **all 17 filterable columns**, data-driven |
| `FilterUserTest` | The advanced "Filter" side drawer (funnel icon) |
| `ViewUserTest` | Read-only "View" drawer |
| `CreateUserTest` | 3-step Add New User flow |
| `EditUserTest` | Editing an existing user's name |
| `ChangeUserPasswordTest` | Change Password modal |
| `DeactivateUserTest` | Deactivate confirmation modal + last-working-day picker |

### Teams module (`tests/teams`)
| Test | Covers |
|---|---|
| `SearchTeamTest` | Search box |
| `TeamValidationTest` | Required-field validation on create |
| `SortTeamTest` | Column header sort — data-driven over the 3 sortable columns (S No, Team Name, Status) |
| `ColumnFilterTeamTest` | AG-Grid's built-in per-column filter menu — data-driven over the 2 filterable columns (Team Name, Status). Note: unlike Users, Teams has **no** advanced "Filter" side drawer, so this is the only filter mechanism for this module. |
| `CreateTeamTest` | Create New Team drawer |
| `EditTeamTest` | Editing an existing team's name |
| `AddTeamMembersTest` | "Add Members" modal — search + select a user + Add |
| `DeactivateTeamTest` | Deactivating a currently-**Active** team (with confirmation prompt) |
| `ActivateTeamTest` | Activating a currently-**Inactive** team (with confirmation prompt) |

### Departments module (`tests/departments`)
Mirrors the Team module (the two share nearly identical UI and even the same Add Members modal component). Only "Department Name" and "Status" columns are sortable/filterable ("S No" is not, unlike Teams).
| Test | Covers |
|---|---|
| `SearchDepartmentTest` | Search box |
| `DepartmentValidationTest` | Required-field + minimum-length (3 chars) validation on create |
| `SortDepartmentTest` | Column header sort — data-driven over the 2 sortable columns (Department Name, Status) |
| `ColumnFilterDepartmentTest` | AG-Grid per-column Set Filter — data-driven over the 2 filterable columns (Department Name, Status) |
| `ViewDepartmentTest` | Read-only "View" drawer |
| `CreateDepartmentTest` | Create New Department drawer |
| `EditDepartmentTest` | Editing an existing department's name |
| `DuplicateDepartmentTest` | Duplicate department-name rejection ("The name has already been taken") |
| `AddDepartmentMembersTest` | "Add Members" modal (reuses `AddTeamMembersModal` — same shared component) |
| `DeactivateDepartmentTest` | Deactivating a currently-**Active** department (with confirmation prompt) |
| `ActivateDepartmentTest` | Activating a currently-**Inactive** department (with confirmation prompt) |

---

## 5. Viewing the HTML report

Every `mvn test` run generates a timestamped **ExtentReports** HTML file (with a screenshot
attached to any failed test):

```
test-output/ExtentReport/ExtentReport_<yyyyMMdd_HHmmss>.html
```

Open it in any browser after the run finishes.

---

## 6. Project structure

```
VisdumAutomation/
├── pom.xml                          # Maven deps: Selenium, TestNG, WebDriverManager, ExtentReports, POI
├── testng-excel.xml                 # Default suite (runs the Excel-driven test) + ExtentReportListener registration
├── testng-datadriven.xml            # Optional data-driven suite (not run by default)
├── testng.xml                       # Optional class-based suite (not run by default)
├── src/test/resources/
│   └── config.properties            # url / username / password for the target environment
└── src/test/java/
    ├── Base/
    │   ├── DriverFactory.java       # Singleton ChromeDriver
    │   ├── BaseTest.java            # Login + navigate to Employees (used by Employees tests)
    │   ├── TeamsBaseTest.java       # Login + navigate to Teams (used by Teams tests)
    │   └── UserModuleTest.java      # Shared fixture: creates a fresh active user
    ├── Pages/                       # Page Objects (one per screen/drawer/modal)
    │   ├── BasePage.java            # Shared click/type/text + react-select helpers
    │   ├── LoginPage.java, DashboardPage.java
    │   ├── UsersPage.java, CreateUserPage.java, UserViewPage.java,
    │   │   ChangePasswordModal.java, DeactivateUserModal.java, UserFilterPage.java
    │   └── TeamsPage.java, TeamFormPage.java, TeamViewPage.java, TeamStatusModal.java,
    │       AddTeamMembersModal.java
    ├── listeners/
    │   └── ExtentReportListener.java  # HTML report + failure screenshots (suite-wide)
    ├── utilities/
    │   ├── ConfigReader.java, ExecutionGuard.java
    │   ├── TestUser.java, TestTeam.java
    │   ├── UserColumn.java          # Source of truth for all Users grid columns
    │   └── TeamColumn.java          # Source of truth for all Teams grid columns
    └── tests/
        ├── employees/               # 10 test classes (see table above)
        ├── teams/                   # 9 test classes (see table above)
        └── departments/             # 11 test classes (see table above)
```

---

## 7. Troubleshooting

| Symptom | Fix |
|---|---|
| `Unknown lifecycle phase "...SomeTest#someMethod"` | You pasted a command that got line-wrapped. Retype it as one line (see §3.3). |
| `Cannot access central (...) in offline mode` | Remove any `-o` / `--offline` flag, or run `mvn -q dependency:resolve` once with internet access. |
| Test throws `IllegalArgumentException: Set -Dtest.user.existing=...` | That test needs an existing record to act on — pass the flag shown in the error (see §3.4). |
| Test skipped with `SkipException: Set -Drun.destructive.tests=true...` | Expected — that test changes data and is opt-in. Add the flag if you intend to run it. |
| ChromeDriver / browser version mismatch | WebDriverManager handles this automatically; just ensure you have internet access on first run. |
| Login step fails immediately | Double-check `url`/`username`/`password` in `config.properties`. |
| Horizontal scrollbar doesn't move / off-screen column (Currency, Employee Id, Manager, etc.) can't be found during `SortUserTest` / `ColumnFilterUserTest` | Fixed in `BasePage.scrollColumnIntoView()`: it scrolls AG-Grid's real virtualized render container (`.ag-center-cols-viewport`) directly. `sortByColumn`, `sortDirectionOf`, and `openColumnMenu` all call this automatically -- just make sure you're on this version of the project. |
| `ColumnFilterUserTest` times out waiting for a button with class `ag-filter-apply-panel-button` / text "Apply" | Fixed: your app's `table/_columns.tsx` doesn't configure `filterParams.buttons`, so AG-Grid's Set Filter has **no Apply/Reset button at all** -- it filters live as soon as a checkbox is toggled. The test now toggles a value off, then toggles it back on, with no button click in between. |
| `BaseTest.setup` throws `TimeoutException` on `driver.get(...)` | Not a code bug -- this is the browser timing out loading `uat.visdum.com` itself (site/network slowness). Re-run in isolation; if it repeats consistently, check your network/VPN access to the UAT environment. |
