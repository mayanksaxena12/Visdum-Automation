package listeners;

// import java.io.ByteArrayInputStream;
// import java.io.File;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import Base.DriverFactory;

/**
 * TestNG listener that produces an ExtentReports Spark HTML report under
 * {@code test-output/ExtentReport/} for every suite run, attaching a screenshot to any failed test.
 *
 * <p>Wired suite-wide via {@code <listeners>} in testng.xml so it covers every test class
 * regardless of which base class (BaseTest / TeamsBaseTest) it extends.
 */
public class ExtentReportListener implements ITestListener {

    private static final ExtentReports extent = createExtentReports();
    private static final Map<Long, ExtentTest> testMap = new ConcurrentHashMap<>();

    private static ExtentReports createExtentReports() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportPath = "test-output/ExtentReport/ExtentReport_" + timestamp + ".html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setDocumentTitle("Visdum Automation Report");
        spark.config().setReportName("Visdum Selenium/TestNG Results");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(spark);
        return reports;
    }

    @Override
    public void onTestStart(ITestResult result) {
        testMap.put(Thread.currentThread().getId(), createTest(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        currentTest(result).log(Status.PASS, "Test passed.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = currentTest(result);
        test.log(Status.FAIL, result.getThrowable());
        attachScreenshot(test);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        currentTest(result).log(Status.SKIP, result.getThrowable() != null
                ? result.getThrowable().getMessage()
                : "Test skipped.");
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    /**
     * Returns the current thread's ExtentTest, creating it on demand. This matters for tests that
     * are skipped/failed because of a configuration method (e.g. a {@code @BeforeMethod} throwing
     * {@code SkipException} in the destructive-test guard): TestNG fires {@code onTestSkipped}
     * without ever calling {@code onTestStart}, so without this fallback {@code currentTest()} would
     * return null and NPE inside the handlers.
     */
    private ExtentTest currentTest(ITestResult result) {
        return testMap.computeIfAbsent(Thread.currentThread().getId(), id -> createTest(result));
    }
 
    private ExtentTest createTest(ITestResult result) {
        return extent.createTest(
                result.getMethod().getQualifiedName(), result.getMethod().getDescription());
    }

    private void attachScreenshot(ExtentTest test) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver == null || !(driver instanceof TakesScreenshot)) {
            return;
        }
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String base64 = Base64.getEncoder().encodeToString(screenshot);
            test.fail("Screenshot on failure",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
        } catch (Exception e) {
            test.log(Status.WARNING, "Could not capture screenshot: " + e.getMessage());
        }
    }
}
