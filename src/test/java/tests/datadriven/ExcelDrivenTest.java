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
 *   <li>All other rows (Login module, and destructive/complex/manual scenarios) are reported as
 *       <b>Skipped</b> with a clear reason -- they are never silently dropped.</li>
 * </ul>
 *
 * <p>Login happens lazily, only for auto-executable rows, so the hundreds of manual rows skip
 * instantly without launching a browser. Default workbook:
 * {@code src/test/resources/manual-testcases.xlsx}; override with {@code -Dexcel.file=<path>}.
 */
public class ExcelDrivenTest implements ITest {

    private static final String DEFAULT_FILE = "src/test/resources/manual-testcases.xlsx";
    private final ThreadLocal<String> currentName = new ThreadLocal<>();

    @DataProvider(name = "excelRows", parallel = true)
    public Object[][] excelRows() {
        String path = System.getProperty("excel.file", DEFAULT_FILE);
        System.out.println(ExcelTestCaseReader.coverageSummary(path));
        List<TestCaseRow> rows = ExcelTestCaseReader.readAll(path);
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    @Test(dataProvider = "excelRows")
    public void execute(TestCaseRow row) throws Exception {
        currentName.set("[" + row.get("Sheet") + "] " + row.getTestCaseId() + " - " + row.getDescription());

        if (!ExcelTestCaseReader.isAutoExecutable(row)) {
            throw new SkipException("Manual/unmapped case (module=" + row.getModule()
                    + ", scenario=" + row.getScenario() + "). No automated mapping -- execute manually.");
        }

        WebDriver driver = DriverFactory.getDriver();
        driver.get(ConfigReader.get("url"));
        // Login-module actions run on the login page itself; every other module needs an
        // authenticated session first.
        if (!row.getModule().equalsIgnoreCase("Login")) {
            new LoginPage(driver).login(ConfigReader.get("username"), ConfigReader.get("password"));
        }

        TestAction action = TestCaseRegistry.get(row.actionKey());
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
