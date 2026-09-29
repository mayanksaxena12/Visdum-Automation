package listeners;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
import com.aventstack.extentreports.reporter.configuration.Theme;

import Base.DriverFactory;
import utilities.TestCaseRow;

/**
 * TestNG listener producing detailed HTML ExtentReports under {@code test-output/ExtentReport/}
 * with embedded Base64 screenshots, saved PNG files under {@code test-output/screenshots/},
 * and test case metadata from the manual Excel file.
 */
public class ExtentReportListener implements ITestListener {

    private static String latestReportPath = "";
    private static final ExtentReports extent = createExtentReports();
    private static final Map<Long, ExtentTest> testMap = new ConcurrentHashMap<>();

    private static ExtentReports createExtentReports() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportDir = "test-output/ExtentReport";
        new File(reportDir).mkdirs();
        latestReportPath = reportDir + "/ExtentReport_" + timestamp + ".html";

        ExtentSparkReporter spark = new ExtentSparkReporter(latestReportPath);
        spark.config().setDocumentTitle("Visdum Automation Report");
        spark.config().setReportName("Visdum Test Execution Results");
        spark.config().setTheme(Theme.STANDARD);

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(spark);
        reports.setSystemInfo("Environment", "UAT");
        reports.setSystemInfo("Application", "Visdum 2.0");
        reports.setSystemInfo("Author", "QA Automation");
        return reports;
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = createTest(result);
        testMap.put(Thread.currentThread().getId(), test);
        System.out.println("\n------------------------------------------------------------");
        System.out.println("[TEST RUNNING] " + test.getModel().getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = currentTest(result);
        System.out.println("[TEST SUCCESS] ✅ " + test.getModel().getName());
        System.out.println("------------------------------------------------------------\n");
        test.log(Status.PASS, "Test passed successfully.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = currentTest(result);
        String reason = result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown Error";
        System.out.println("[TEST FAILED] ❌ " + test.getModel().getName());
        System.out.println("  Failure Reason: " + reason);
        System.out.println("------------------------------------------------------------\n");

        test.log(Status.FAIL, result.getThrowable());
        attachScreenshot(test, result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = currentTest(result);
        String reason = result.getThrowable() != null ? result.getThrowable().getMessage() : "Test skipped.";
        System.out.println("[TEST SKIPPED] ⚠️ " + test.getModel().getName() + " | Reason: " + reason);
        System.out.println("------------------------------------------------------------\n");
        test.log(Status.SKIP, reason);
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
        try {
            File source = new File(latestReportPath);
            File dest = new File("test-output/ExtentReport/ExtentReport_Latest.html");
            if (source.exists()) {
                Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {
        }

        System.out.println("\n============================================================");
        System.out.println("📊 EXTENT REPORT GENERATED SUCCESSFULLY!");
        System.out.println("📁 Report File : " + new File(latestReportPath).getAbsolutePath());
        System.out.println("🔗 Latest Copy : " + new File("test-output/ExtentReport/ExtentReport_Latest.html").getAbsolutePath());
        System.out.println("📷 Screenshots : " + new File("test-output/screenshots").getAbsolutePath());
        System.out.println("============================================================\n");
    }

    private ExtentTest currentTest(ITestResult result) {
        return testMap.computeIfAbsent(Thread.currentThread().getId(), id -> createTest(result));
    }

    private ExtentTest createTest(ITestResult result) {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof TestCaseRow row) {
            String testTitle = "[" + row.get("Sheet") + "] " + row.getTestCaseId() + " : " + row.getDescription();
            ExtentTest test = extent.createTest(testTitle, "Scenario: " + row.getScenario() + " | Module: " + row.getModule());
            test.assignCategory(row.get("Sheet"));
            test.assignCategory(row.getModule());

            if (row.getData() != null && !row.getData().isEmpty()) {
                StringBuilder sb = new StringBuilder("<div style='font-size:12px; margin-top:5px;'>");
                row.getData().forEach((k, v) -> {
                    if (v != null && !v.isBlank()) {
                        sb.append("<b>").append(k).append(":</b> ").append(v).append(" | ");
                    }
                });
                sb.append("</div>");
                test.info(sb.toString());
            }
            return test;
        }

        String name = result.getName();
        if (result.getInstance() instanceof org.testng.ITest iTest && iTest.getTestName() != null) {
            name = iTest.getTestName();
        }
        return extent.createTest(name, result.getMethod().getDescription());
    }

    private void attachScreenshot(ExtentTest test, ITestResult result) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver == null || !(driver instanceof TakesScreenshot)) {
            return;
        }
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String base64 = Base64.getEncoder().encodeToString(screenshot);

            // 1. Embed screenshot directly in ExtentReport HTML
            test.fail("<b>Screenshot on Failure:</b>",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());

            // 2. Also save physical .png file to test-output/screenshots/
            String screenshotsDir = "test-output/screenshots";
            new File(screenshotsDir).mkdirs();
            String tcId = "failure";
            Object[] params = result.getParameters();
            if (params != null && params.length > 0 && params[0] instanceof TestCaseRow row) {
                tcId = row.getTestCaseId().replaceAll("[^a-zA-Z0-9-_]", "_");
            }
            String fileName = tcId + "_" + System.currentTimeMillis() + ".png";
            File destFile = new File(screenshotsDir, fileName);
            Files.write(destFile.toPath(), screenshot);
            System.out.println("[SCREENSHOT CAPTURED] " + destFile.getAbsolutePath());
        } catch (Exception e) {
            test.log(Status.WARNING, "Could not capture screenshot: " + e.getMessage());
        }
    }
}
