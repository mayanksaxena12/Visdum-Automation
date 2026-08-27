# Framework Architecture & Data-Driven Execution

This document explains the automation framework's architecture, the data-driven execution flow,
the performance model, and **how to add new test cases without writing code**.

---

## 1. High-level architecture

```
                       ┌──────────────────────────────┐
                       │   Control file (XLSX only)   │   <- test DATA (no logic)
                       │   manual-testcases.xlsx      │
                       └──────────────┬───────────────┘
                                      │ read all sheets                                      ▼
          ExcelTestCaseReader ──► TestCaseRow ──► ExcelDrivenTest (@DataProvider)
                                                      │ dispatch by Module:Scenario
                                                      ▼
                                            TestCaseRegistry ──► TestAction
                                                      │ uses
                                                      ▼
                          Page Objects (UsersPage / TeamsPage / DepartmentsPage / …)
                                                      │ drive
                                                      ▼
                          DriverFactory (ThreadLocal WebDriver)  ──► Chrome
```

- **Test data** lives entirely in the Excel workbook. **Test logic** lives in Page Objects and the
  registry. The two never mix — this is the core of the data-driven design.
- The classic per-class tests (`tests/employees`, `tests/teams`, `tests/departments`) still exist
  and are unchanged; the Excel-driven runner is an additional execution path over the same Page
  Objects.

---

## 2. Key components

| Component | Responsibility |
|---|---|
| `utilities/TestCaseReader` | Reads the control file (`.csv` or `.xlsx` via Apache POI). Maps columns by **header name** (order-independent). Returns only `Run=Yes` rows via `readRunnable()`. |
| `utilities/TestCaseRow` | Immutable model of one row. Exposes `param1()`, `param2()`, `get("Col")`, and `actionKey()` = `Module:Scenario`. |
| `utilities/TestCaseRegistry` | Maps `Module:Scenario` → `TestAction` (a lambda built from Page Objects). Dispatch is **not** keyed by individual Test Case ID, so reusing a scenario needs no code. |
| `utilities/TestAction` | Functional interface: `run(WebDriver, TestCaseRow)`. |
| `tests/datadriven/DataDrivenTest` | TestNG runner. `@DataProvider` yields `Run=Yes` rows; `@Test` dispatches each to the registry. Implements `ITest` so each row is named by its Test Case ID in the report. |
| `Base/DriverFactory` | **Thread-safe** `ThreadLocal<WebDriver>` provider — each parallel thread gets its own isolated Chrome. |
| `listeners/ExtentReportListener` | Thread-safe ExtentReports (Spark) HTML report with per-failure screenshots. |

---

## 3. Execution flow (data-driven)

1. `DataDrivenTest.@DataProvider` calls `TestCaseReader.readRunnable(path)`.
   - Path defaults to `src/test/resources/datadriven-testcases.csv`; override with
     `-Dtestcase.file=<path>` (`.csv` or `.xlsx`).
   - Rows whose `Run` column is **not** Yes/Y/True/1 are filtered out.
2. For each remaining row, `@BeforeMethod` logs in (shared login flow), then `@Test` builds the
   dispatch key `Module:Scenario` and looks up the `TestCaseRegistry`.
3. The matched `TestAction` navigates to the correct module page and performs the check using the
   existing Page Objects, reading `Param1`/`Param2` from the row for its data.
4. If no action is mapped for a row, the test is **skipped** with a clear message (not failed).
5. `@AfterMethod` quits that thread's driver.

---

## 4. How to add a new test case (NO code change)

If the new case reuses an existing **scenario type** (Search, Sort, ColumnFilter, View), just add a
row to the control file:

```csv
TestCaseId,Module,Scenario,Description,Run,Param1,Param2
DD_U_08,User,Sort,Sort Users by Currency column,Yes,currency,
DD_D_06,Department,ColumnFilter,Set Filter on Status,Yes,status,
```

- `Param1` carries the scenario's data (a search term, or an AG-Grid `col-id` such as
  `name`, `email`, `status`, `role`, `currency`, `employee_number`, …).
- Set `Run=No` to disable a case without deleting it.
- Save the file and re-run — no recompilation needed.

### Adding a brand-new scenario TYPE (one-time code change)
If you need a scenario that doesn't exist yet (e.g. `User:BulkExport`), add one entry to
`TestCaseRegistry`:

```java
register("user:bulkexport", (driver, row) -> {
    new DashboardPage(driver).navigateToEmployees();
    // ...use Page Objects, assert with row.param1()/param2()...
});
```
Then reference it from the control file with `Module=User, Scenario=BulkExport`.

### Using your own Excel file
Point the runner at any `.xlsx` whose first sheet has the header row
`TestCaseId, Module, Scenario, Description, Run, Param1, Param2` (extra columns are preserved):
```
mvn test -DsuiteXmlFile=testng-datadriven.xml -Dtestcase.file=/path/to/your-file.xlsx
```

---

## 5. Performance model

- **Parallel class execution** (`testng.xml`: `parallel="classes" thread-count="3"`) — read-only
  test classes run 3 at a time. Each thread gets its own driver via `ThreadLocal`, so there is no
  shared-state corruption.
- **Parallel data rows** (`testng-datadriven.xml`: `data-provider-thread-count="3"` +
  `@DataProvider(parallel=true)`) — data-driven rows run concurrently.
- **Explicit waits only** — `BasePage` uses `WebDriverWait` (20s) with `ExpectedConditions`
  throughout; there are no `Thread.sleep()` static waits in the page/test logic. (The one bounded
  loop pause is in `scrollColumnIntoView`, pacing AG-Grid's virtualization re-render.)
- **Bounded page-load timeout** (60s) in `DriverFactory` prevents indefinite hangs.
- **No duplicate code** — grid sort/filter/scroll helpers live once in `BasePage`; the shared
  Add-Members modal Page Object (`AddTeamMembersModal`) is reused by both Team and Department tests.

> ⚠️ **Destructive runs & parallelism:** create/edit/deactivate/activate/add-members tests mutate
> shared UAT data under a single login account. When running them
> (`-Drun.destructive.tests=true`), set `thread-count="1"` in `testng.xml` or run those classes
> individually to avoid concurrent-mutation conflicts. The default (read-only) run is safe at
> thread-count 3.

---

## 6. Reporting

- ExtentReports Spark HTML report is generated at
  `test-output/ExtentReport/ExtentReport_<timestamp>.html` on every run.
- Includes Pass / Fail / Skip status, per-test timing, exception stack traces, and a screenshot
  attached to every failure.
- Under parallel execution the listener keys results by thread id (`ConcurrentHashMap`), so
  concurrent tests are reported correctly.
- Data-driven rows appear under their Test Case ID + description (via `ITest`).

---

## 7. Run commands (quick reference)

```
# Full class-based suite (parallel, read-only safe; destructive auto-skips)
mvn clean test

# Data-driven suite (reads control file, Run=Yes only, parallel rows)
mvn test -DsuiteXmlFile=testng-datadriven.xml

# Data-driven against your own Excel file
mvn test -DsuiteXmlFile=testng-datadriven.xml -Dtestcase.file=/path/to/your-file.xlsx

# Excel-driven: read & execute EVERY sheet of the QA manual workbook
mvn test -DsuiteXmlFile=testng-excel.xml
mvn test -DsuiteXmlFile=testng-excel.xml -Dexcel.file=/path/to/workbook.xlsx

# Include destructive tests (set thread-count=1 in testng.xml first)
mvn clean test -Drun.destructive.tests=true -Dtest.user.existing="Mayank" -Dtest.team.existing="Sales" -Dtest.department.existing="Sales Engineers"
```

---

## 8. Excel workbook integration (`ExcelTestCaseReader` + `ExcelDrivenTest`)

The QA team's manual workbook is wired in directly. `ExcelTestCaseReader` reads **every sheet**,
locates each sheet's header row (it differs per sheet), maps columns by header name, and maps each
sheet to a module:

| Sheet | Module | Automation |
|---|---|---|
| Login | Login | Field validation, invalid-credentials, unregistered-email & valid→2FA-redirect auto-run; OTP/2FA-completion, resend, forgot-password, SSO & profile remain manual |
| Users | User | Search / Sort / ColumnFilter / View auto-run; rest manual |
| Team | Team | Search / Sort / ColumnFilter / View auto-run; rest manual |
| Departments | Department | Search / Sort / ColumnFilter / View auto-run; rest manual |

Auto-executable coverage after Login automation was added: **116 of 601 rows** (Login 25, User 53,
Team 15, Department 23). The remaining rows are genuinely manual (OTP-gated flows, or free-text UI
checks that no tool can execute verbatim).

Each row's free-text **Test Scenario** is classified into a scenario token; if
`Module:Scenario` has a registered automation (and is a safe read-only scenario), it runs live —
otherwise the row is reported as **Skipped (manual)** with a reason. No sheet or row is dropped:
every row appears in the report, and a coverage summary is printed at run start.

> **Why not auto-run all 601 rows?** The workbook is a *human* test tracker with free-text steps
> (e.g. "Verify member avatars overflow: show up to visible count then +X indicator"). Those can't
> be executed verbatim by any tool. The framework auto-runs the rows that map to a concrete,
> repeatable UI action and clearly flags the rest for manual execution. To automate more, add the
> scenario to `TestCaseRegistry` (see §4) — the Excel row then auto-runs with no further change.

Default lookup terms for Search/View can be overridden:
`-Dexcel.user.search=... -Dexcel.team.search=... -Dexcel.department.search=...`
