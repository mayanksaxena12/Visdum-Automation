package tests.plans;

import Base.DriverFactory;
import com.aventstack.extentreports.Status;
import listeners.ExtentReportListener;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.ScreenRecorderUtil;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Driven TestNG Runner for all 120 Test Cases of Ticket V20-1717: Force End Plan Workflow.
 * Reads directly from src/test/resources/Force_End_Plan_120_Test_Cases.xlsx.
 * Runs in Headless mode, capturing live frames and embedding HTML5 video & screenshots.
 */
public class ForceEndPlanDataDrivenTest implements ITest {

    private final ThreadLocal<String> testName = new ThreadLocal<>();

    @Override
    public String getTestName() {
        return testName.get() != null ? testName.get() : "ForceEndPlanTest";
    }

    public static class FepTestCase {
        public String id;
        public String area;
        public String scenario;
        public String preconditions;
        public String steps;
        public String expectedResult;
        public String actualResult;
        public String status;
        public String priority;
        public String remarks;

        @Override
        public String toString() {
            return "[" + id + "] " + scenario;
        }
    }

    @DataProvider(name = "forceEndPlanCases")
    public Object[][] getForceEndPlanCases() {
        List<FepTestCase> list = new ArrayList<>();
        File excelFile = new File("src/test/resources/Force_End_Plan_120_Test_Cases.xlsx");
        if (!excelFile.exists()) {
            excelFile = new File("../src/test/resources/Force_End_Plan_120_Test_Cases.xlsx");
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String id = formatter.formatCellValue(row.getCell(0)).trim();
                if (id.isEmpty()) continue;

                FepTestCase tc = new FepTestCase();
                tc.id = id;
                tc.area = formatter.formatCellValue(row.getCell(1)).trim();
                tc.scenario = formatter.formatCellValue(row.getCell(2)).trim();
                tc.preconditions = formatter.formatCellValue(row.getCell(3)).trim();
                tc.steps = formatter.formatCellValue(row.getCell(4)).trim();
                tc.expectedResult = formatter.formatCellValue(row.getCell(5)).trim();
                tc.actualResult = formatter.formatCellValue(row.getCell(6)).trim();
                tc.status = formatter.formatCellValue(row.getCell(7)).trim();
                tc.priority = formatter.formatCellValue(row.getCell(8)).trim();
                tc.remarks = formatter.formatCellValue(row.getCell(9)).trim();

                list.add(tc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to read Force_End_Plan_120_Test_Cases.xlsx: " + e.getMessage());
        }

        Object[][] data = new Object[list.size()][1];
        for (int i = 0; i < list.size(); i++) {
            data[i][0] = list.get(i);
        }
        return data;
    }

    @Test(dataProvider = "forceEndPlanCases")
    public void testForceEndPlanCase(FepTestCase tc) {
        String fullName = "[" + tc.id + "] " + tc.area + " - " + tc.scenario;
        testName.set(fullName);

        System.out.println("====================================================");
        System.out.println("Executing Test Case: " + fullName);
        System.out.println("Priority: " + tc.priority + " | Preconditions: " + tc.preconditions);
        System.out.println("Steps: " + tc.steps);
        System.out.println("Expected Result: " + tc.expectedResult);
        System.out.println("Actual Result: " + tc.actualResult);
        System.out.println("====================================================");

        // P0 audit fix: this row used to PASS or FAIL based only on the Status cell a person typed
        // into the spreadsheet; nothing touched the product. Report it honestly as not automated,
        // carrying the recorded manual result so the matrix stays visible in the report.
        throw new SkipException("Not automated (manual matrix row). Recorded manual status: '"
                + tc.status + "'" + (tc.remarks == null || tc.remarks.isBlank() ? "" : ". Remarks: " + tc.remarks));
    }
}
