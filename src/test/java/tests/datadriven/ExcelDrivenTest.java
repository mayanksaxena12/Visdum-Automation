package tests.datadriven;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.testng.ITest;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import Base.DriverFactory;
import Pages.LoginPage;
import utilities.ConfigReader;
import utilities.ExcelTestCaseReader;
import utilities.TestAction;
import utilities.TestCaseRegistry;
import utilities.TestCaseRow;

/**
 * Executes the QA team's manual Excel workbook (all sheets) through the automation framework.
 *
 * <p>Every sheet is mapped to a module and every row is read and represented in the report:
 * <ul>
 *   <li>Rows whose classified {@code Module:Scenario} has automation (User/Team/Department
 *       Search, Sort, ColumnFilter, View) are executed live.</li>
 *   <li>Login rows that reach the 2-step verification screen are executed live too: the OTP is
 *       read from the UAT database ({@code two_factor_authentications}, latest row via
 *       {@code ORDER BY id DESC}) so the full OTP flow can be automated.</li>
 *   <li>All other rows (OTP edge cases without a mapping, and destructive/complex/manual
 *       scenarios) are reported as <b>Skipped</b> with a clear reason -- they are never
 *       silently dropped.</li>
 * </ul>
 *
 * <p>Login happens lazily, only for auto-executable rows, so the hundreds of manual rows skip
 * instantly without launching a browser. Default workbook:
 * {@code src/test/resources/manual-testcases.xlsx}; override with {@code -Dexcel.file=<path>}.
 */
public class ExcelDrivenTest implements ITest {

    private static final String DEFAULT_FILE = "src/test/resources/manual-testcases.xlsx";
    private final ThreadLocal<String> currentName = new ThreadLocal<>();

    @DataProvider(name = "excelRows")
    public Object[][] excelRows() {
        String path = System.getProperty("excel.file", DEFAULT_FILE);
        System.out.println(ExcelTestCaseReader.coverageSummary(path));
        // MANDATE: Read 100% of the test cases from the Excel workbook regardless of Run column value.
        List<TestCaseRow> rows = ExcelTestCaseReader.readAll(path);

        if (rows.isEmpty()) {
            String filterSheet = System.getProperty("excel.sheet", "").trim();
            String msg = "No test cases found in Excel"
                    + (filterSheet.isEmpty() ? "" : " sheet '" + filterSheet + "'")
                    + " (" + path + ").";
            System.out.println("[EXCEL RUNNER] " + msg);
            java.util.Map<String, String> data = new java.util.LinkedHashMap<>();
            data.put("Sheet", filterSheet.isEmpty() ? "All" : filterSheet);
            TestCaseRow infoRow = new TestCaseRow("EXCEL-00", "Info", "info", msg, false, data);
            return new Object[][] { { infoRow } };
        }

        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    @Test(dataProvider = "excelRows")
    public void execute(TestCaseRow row) throws Exception {
        String testTitle = "[" + row.get("Sheet") + "] " + row.getTestCaseId() + " - " + row.getDescription();
        currentName.set(testTitle);

        System.out.println("\n------------------------------------------------------------");
        System.out.println("[EXCEL ROW RUNNING] " + testTitle);
        System.out.println("  Module: " + row.getModule() + " | Scenario: " + row.getScenario() + " | ActionKey: " + row.actionKey()
                + " | RunMetadata: " + row.get("RunMetadata"));

        // Decide first: a row with no automation is skipped without launching Chrome or logging in
        // (previously every one of these rows cost a full browser start + login before skipping).
        TestAction action = TestCaseRegistry.get(row.actionKey());
        if (action == null) {
            throw new SkipException("Skipped (manual): no automation mapped for '" + row.actionKey()
                    + "' (" + row.getTestCaseId() + "). Add an entry to TestCaseRegistry to automate it.");
        }

        WebDriver driver = DriverFactory.getDriver();
        driver.get(ConfigReader.get("url"));

        // If organization module: login using Super Admin credentials.
        // If login module: runs directly on the login page.
        // Other business modules: login using standard tenant admin credentials.
        if (row.getModule().equalsIgnoreCase("Organization")) {
            String superUser = ConfigReader.get("super_admin.username", ConfigReader.get("username"));
            String superPass = ConfigReader.get("super_admin.password", ConfigReader.get("password"));
            new LoginPage(driver).login(superUser, superPass);
        } else if (!row.getModule().equalsIgnoreCase("Login")) {
            new LoginPage(driver).login(ConfigReader.get("username"), ConfigReader.get("password"));
        }

        action.run(driver, row);
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }

    @Override
    public String getTestName() {
        return currentName.get() == null ? "ExcelDrivenTest" : currentName.get();
    }
}
