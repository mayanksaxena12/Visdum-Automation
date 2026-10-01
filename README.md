# Visdum Automation Suite

Selenium + Java 17 + TestNG enterprise UI automation framework for **Visdum** (`visdum-v2-frontend` & `visdum-v2-backend`). Built with Maven, ExtentReports for interactive HTML reporting, zero-codec HTML5 Video Replay recording, and WebDriverManager for automatic driver management.

---

## 1. Supported Modules & Test Coverage

| Module | Route / Component | Page Objects | Key Capabilities Covered |
|---|---|---|---|
| **Employees / Users** | `/employees` | `UsersPage`, `CreateUserPage`, `UserViewPage`, `UserFilterPage`, `ChangePasswordModal`, `DeactivateUserModal` | Search, Sort (17 cols), Column Set Filter, Side Drawer Filter, 3-step User creation, Edit, Deactivate, Password Change, Fetch Users integration, User Data Streams |
| **Teams** | `/teams` | `TeamsPage`, `TeamFormPage`, `TeamViewPage`, `TeamStatusModal`, `AddTeamMembersModal` | Search, Sort (3 cols), Column Set Filter, Create, Edit, Add/Remove Team Members, Deactivate/Activate |
| **Departments** | `/departments` | `TeamsPage` (shared abstraction), `TeamFormPage`, `TeamViewPage`, `TeamStatusModal` | Search, Sort (2 cols), Column Set Filter, Create, Edit, Duplicate name detection, Add Members, Deactivate/Activate |
| **Data Streams** | `/data/data-streams` & `/data-streams/add` | `DataStreamsPage`, `CreateDataStreamPage`, `AgGridListPage` | Active/Drafts tabs, Search, Sort (6 cols), Set Filter, Read-only Drawer, Multi-step Wizard for Earning, Payout, Reference, Key-Value; Connected Apps, Upload Sheet, Base View; History Tracking, Status transitions |
| **Raw Data** | `/data/raw-data` | `RawDataPage`, `AgGridListPage` | Stream selection dropdown, AG-Grid records validation, Dynamic column filtering, data export |
| **Rate Tables** | `/plans/rate-table` & `/rate-table/add` | `RateTablePage`, `CreateRateTableWizardPage` | Rate Table list grid, Search, Column Sort & Filters, Add Row, File Upload dropzone, Template Download, 4-step Creation Wizard (Details, Mapping, Lookup Rule, Review), Edit Rate Table |
| **Create Plan** | `/plans/create-plan` & `/plans/create-plan/add` | `PlansPage`, `CreatePlanWizardPage`, `ExpressionBuilderHelper` | Active/Draft/Archived tabs, Search, Sort, 4-step Plan Creation Wizard (Plan Details, Components, Expression Rule Builder, Review), Edit, Clone, Delete, Status change |
| **Authentication** | `/login` | `LoginPage`, `BaseTest` | Yup validations, invalid credentials, redirect flow to 2-step verification, automated session management |

---

## 2. Prerequisites

| Tool | Version | Check with |
|---|---|---|
| Java (JDK) | 17+ | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| Google Chrome | latest | installed locally |

WebDriverManager auto-downloads the matching ChromeDriver binary on first run — no manual driver configuration needed.

---

## 3. Configuration

Edit **`src/test/resources/config.properties`**:

```properties
url=https://uat.visdum.com
username=your.uat.user@example.com
password=YourPassword123
headless=true
browser=chrome
run.destructive.tests=false
```

- `headless=true`: Runs tests silently in the background without opening browser windows.
- `run.destructive.tests=true`: Required to run tests that create, edit, deactivate, or delete application data.

---

## 4. Running Tests

### 4.1 Quick Module Runner (Windows CMD / PowerShell)

We provide handy batch & PowerShell scripts to run individual modules or the entire test suite:

```cmd
:: Run Rate Tables
.\run-module.bat ratetable

:: Run Plans & Create Plan Wizard
.\run-module.bat plans

:: Run Data Streams & Raw Data
.\run-module.bat data

:: Run Users / Employees
.\run-module.bat users

:: Run Teams
.\run-module.bat teams

:: Run Departments
.\run-module.bat departments

:: Run Right-Click Excel Export Across ALL Modules (Users, Teams, Depts, Raw Data, Deal Credits, Rate Tables, Plans, Assign Plans)
.\run-module.bat export

:: Run All Modules
.\run-module.bat all
```

Or using PowerShell directly:
```powershell
.\run-module.ps1 -Module export
.\run-module.ps1 -Module ratetable
.\run-module.ps1 -Module plans -Headless $true
```

### 4.2 Excel-Driven Execution (Default `mvn test`)
The default Maven command executes tests defined in `src/test/resources/manual-testcases.xlsx` via `testng-excel.xml`:
```bash
mvn test
```
The Excel runner parses module sheets (Employees, Teams, Departments, Data Streams, Rate Tables, Create Plan), maps scenarios to automated actions in `TestCaseRegistry`, executes them, and logs unautomated complex flows as Skipped with detailed reasons.

### 4.3 Running Specific Test Classes
```bash
# Data Streams
mvn test -Dtest=SearchDataStreamTest
mvn test -Dtest=CreateDataStreamTest -Drun.destructive.tests=true

# Create Plan & Rate Table
mvn test -Dtest=PlansPageTest
mvn test -Dtest=RateTablePageTest
```

### 4.4 Running Destructive Tests (Create / Edit / Delete)
Destructive tests are protected by `ExecutionGuard` to avoid unintended modifications on shared environments:
```bash
# Create a new user
mvn test -Dtest=CreateUserTest -Drun.destructive.tests=true

# Edit an existing user
mvn test -Dtest=EditUserTest -Drun.destructive.tests=true -Dtest.user.existing="Mayank"

# Create a new Rate Table
mvn test -Dtest=CreateRateTableTest -Drun.destructive.tests=true

# Create a new Compensation Plan
mvn test -Dtest=CreatePlanTest -Drun.destructive.tests=true
```

### 4.5 AG-Grid Right-Click Excel Export Suite
You can execute a dedicated test run that right-clicks on the AG-Grid of every implemented module, clicks **Export** -> **Excel Export**, and triggers the spreadsheet download:

```bash
# Windows Batch
.\run-module.bat export

# PowerShell
.\run-module.ps1 -Module export

# Maven Suite directly
mvn test -DsuiteXmlFile=testng-export.xml

# Headless execution
mvn test -DsuiteXmlFile=testng-export.xml -Dheadless=true
```

**Modules covered by `testng-export.xml` (`AgGridExportAllModulesTest`):**
- **Users / Employees** (`/users/employees`)
- **Teams** (`/users/teams`)
- **Departments** (`/users/department`)
- **Raw Data** (`/data/raw-data`)
- **Deal Credits** (`/data/deal-credits`)
- **Rate Tables** (`/plans/rate-table`)
- **Plans** (`/plans/create-plan`)
- **Assign Plans** (`/plans/assign-plan`)

Exported files are automatically saved to `test-output/downloads/` and each module's export is recorded with interactive HTML5 video replay in ExtentReports.

---

## 5. Video Recording & Interactive HTML5 Replay

The framework includes a zero-codec, headless-compatible **HTML5 Video Replay** system (`ScreenRecorderUtil`):

- **Headless-Friendly**: Takes continuous browser DOM snapshots directly via WebDriver `TakesScreenshot` (bypassing the black/blank screen issue of traditional OS display capture in headless mode).
- **Interactive Controls**:
  - ▶ Play / ⏸ Pause
  - ⏮ Previous / ⏭ Next Step frame-by-frame inspection
  - 🎚 Timeline scrubber slider
  - ⚡ Playback Speed Toggle (0.5x, 1x, 2x)
  - 🏷 Action step overlay label & timestamp for each screen
- **Report Integration**: Each executed test generates an HTML replay embedded directly inside the **ExtentReports** dashboard (`test-output/ExtentReport/ExtentReport_<timestamp>.html`) and saved to `test-output/videos/`.

---

## 6. Architecture & Alignment with Visdum Frontend / Backend

### 6.1 Rate Table (`/plans/rate-table` & `/rate-table/add`)
- **Frontend**: `RateTableWrapper.tsx`, `RateTableListHeader.tsx`, `_CreateRateTable.tsx` (4-step wizard).
- **Backend**: `RateController.php` (`fetchRateData`, `storeRateTable`, `storeRateMappers`, `storeLookUpRule`, `savePreview`, `uploadRateData`).
- **Automation Support**:
  - `RateTablePage.java`: Grid search, sort, column filters, action bar (`clickCreateRateTable`, `clickAddRow`, `clickUploadFile`, `clickDownload`, `clickEditRateTable`, `clickDeleteRateTable`).
  - `CreateRateTableWizardPage.java`: Full step navigation (`next()`, `previous()`, `saveToDraft()`, `cancel()`, `submit()`), input form bindings (`fillStep1Details`, file dropzone upload), and validation alerts.

### 6.2 Create Plan (`/plans/create-plan` & `/plans/create-plan/add`)
- **Frontend**: `PlansWrapper.tsx`, `_CreatePlan.tsx` (4-step wizard).
- **Backend**: `PlanMasterController.php` (`getAllAvailablePlan`, `storePlanMaster`, `storePlanComponent`, `storePlanComponentRule`).
- **Automation Support**:
  - `PlansPage.java`: Active, Draft, Archived tabs; Search, Sort, Column filters, Clone, Edit, and Status toggle.
  - `CreatePlanWizardPage.java`: Multi-step form completion (Plan details, Frequency, Components, Expression Rule Builder, Review).
  - `ExpressionBuilderHelper.java`: Automates React condition blocks, operators, and target fields.

### 6.3 Data Streams (`/data/data-streams` & `/data-streams/add`)
- **Frontend**: `DataStreamsWrapper.tsx`, `CreateStreamWrapper.tsx`, `RawDataWrapper.tsx`.
- **Backend**: `DataStreamController.php`.
- **Automation Support**:
  - `DataStreamsPage.java`: Active/Draft stream lists, status badges, history tracking.
  - `CreateDataStreamPage.java`: Wizard handling across Connected Apps, Upload Sheets, and Base Views for all stream types (Earning, Payout, Reference, Key-Value).
  - `RawDataPage.java`: Dynamic record verification per data stream.

---

## 7. Project Structure

```
VisdumAutomation/
├── pom.xml                               # Maven build configuration
├── run-module.bat                        # Quick batch runner for Windows CMD
├── run-module.ps1                        # Quick PowerShell runner
├── testng-excel.xml                      # Default suite (Excel-driven)
├── testng.xml                            # Class-based suite
├── src/test/resources/
│   ├── config.properties                 # Environment configuration (url, credentials, headless)
│   └── manual-testcases.xlsx             # QA test case workbook
└── src/test/java/
    ├── Base/
    │   ├── DriverFactory.java            # ThreadSafe ChromeDriver management
    │   ├── BaseTest.java                 # Standard BaseTest with login fixture
    │   ├── DataStreamsBaseTest.java      # BaseTest for Data Streams navigation
    │   └── TeamsBaseTest.java            # BaseTest for Teams & Departments navigation
    ├── Pages/                            # Page Objects
    │   ├── BasePage.java                 # Shared Selenium actions & React-select helpers
    │   ├── AgGridListPage.java           # Standardized AG-Grid table interaction base
    │   ├── LoginPage.java                # Authentication Page
    │   ├── DashboardPage.java            # Main Navigation & Module links
    │   ├── UsersPage.java                # Users / Employees Grid & Actions
    │   ├── CreateUserPage.java           # 3-step User creation form
    │   ├── TeamsPage.java                # Teams Grid & Actions
    │   ├── DataStreamsPage.java          # Data Streams Grid & Management
    │   ├── CreateDataStreamPage.java     # Multi-step Data Stream wizard
    │   ├── RawDataPage.java              # Raw Data verification
    │   ├── RateTablePage.java            # Rate Table Grid & Actions
    │   ├── CreateRateTableWizardPage.java# 4-step Rate Table wizard
    │   ├── PlansPage.java                # Plans Grid & Management
    │   ├── CreatePlanWizardPage.java     # 4-step Plan wizard
    │   └── ExpressionBuilderHelper.java  # Expression Builder logic
    ├── listeners/
    │   └── ExtentReportListener.java     # ExtentReports listener + Screenshot + HTML5 Video integration
    ├── utilities/
    │   ├── ConfigReader.java             # Property loader
    │   ├── ExcelTestCaseReader.java      # Excel parser & sheet classifier
    │   ├── TestCaseRegistry.java         # Action router mapping test cases to Page Object calls
    │   ├── ScreenRecorderUtil.java       # HTML5 video recorder & frame stitcher
    │   └── ExecutionGuard.java           # Safety guard for destructive test cases
    └── tests/                            # Test classes organized by module
        ├── employees/
        ├── teams/
        ├── departments/
        └── datastreams/
```

---

## 8. Viewing Test Results & Reports

After running tests, two primary reporting artifacts are generated:

1. **ExtentReports HTML Report**:
   ```
   test-output/ExtentReport/ExtentReport_<yyyyMMdd_HHmmss>.html
   ```
   Contains test status summaries, logs, failure stack traces, screenshots, and embedded video replay players.

2. **Standalone HTML5 Video Replays**:
   ```
   test-output/videos/<TestName>_<STATUS>_<timestamp>_replay.html
   ```
   Self-contained interactive video players that can be opened in any browser to inspect every UI step visually.
