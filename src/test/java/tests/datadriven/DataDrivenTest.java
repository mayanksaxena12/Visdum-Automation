package tests.datadriven;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.testng.ITest;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import Base.DriverFactory;
import Pages.LoginPage;
import utilities.ConfigReader;
import utilities.TestAction;
import utilities.TestCaseReader;
import utilities.TestCaseRegistry;
import utilities.TestCaseRow;

/**
 * Data-driven runner. Reads the external control file, executes ONLY rows marked Run=Yes, and
 * dispatches each to its {@link TestCaseRegistry} action by {@code Module:Scenario}. Test logic
 * lives in the Page Objects / registry; test data lives entirely in the control file, so test cases
 * can be added, edited, or toggled with no code change.
 *
 * <p>The control-file path defaults to {@code src/test/resources/datadriven-testcases.csv} and can
 * be overridden with {@code -Dtestcase.file=<path>} (supports .csv or .xlsx). Implements
 * {@link ITest} so each data row shows under its own Test Case ID in the ExtentReport.
 */
public class DataDrivenTest implements ITest {

    private static final String DEFAULT_FILE = "src/test/resources/datadriven-testcases.csv";
    private final ThreadLocal<String> currentName = new ThreadLocal<>();

    @DataProvider(name = "testCases", parallel = true)
    public Object[][] testCases() {
        String path = System.getProperty("testcase.file", DEFAULT_FILE);
        List<TestCaseRow> rows = TestCaseReader.readRunnable(path);
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    @BeforeMethod
    public void login() {
        WebDriver driver = DriverFactory.getDriver();
        driver.get(ConfigReader.get("url"));
        new LoginPage(driver).login(ConfigReader.get("username"), ConfigReader.get("password"));
    }

    @Test(dataProvider = "testCases")
    public void execute(TestCaseRow row) throws Exception {
        currentName.set(row.getTestCaseId() + " - " + row.getDescription());

        TestAction action = TestCaseRegistry.get(row.actionKey());
        if (action == null) {
            throw new SkipException("No automation mapped for '" + row.actionKey()
                    + "' (test case " + row.getTestCaseId() + "). Add an entry to TestCaseRegistry "
                    + "or correct the Module/Scenario in the control file.");
        }
        action.run(DriverFactory.getDriver(), row);
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }

    @Override
    public String getTestName() {
        return currentName.get() == null ? "DataDrivenTest" : currentName.get();
    }
}
