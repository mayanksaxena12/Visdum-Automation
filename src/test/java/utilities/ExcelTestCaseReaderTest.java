package utilities;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ExcelTestCaseReaderTest {

    @Test
    public void testReadResourcesSheet() {
        String path = "src/test/resources/manual-testcases.xlsx";
        List<TestCaseRow> rows = ExcelTestCaseReader.readAll(path);
        
        long resourceRows = rows.stream()
                .filter(r -> "Resource".equalsIgnoreCase(r.getModule()))
                .count();
        
        System.out.println("Total rows read: " + rows.size());
        System.out.println("Resource rows read: " + resourceRows);
        System.out.println(ExcelTestCaseReader.coverageSummary(path));
        
        Assert.assertEquals(resourceRows, 44, "Expected exactly 44 Resource test cases in manual-testcases.xlsx");
        
        TestCaseRow tc01 = rows.stream()
                .filter(r -> "TC-RES-01".equals(r.getTestCaseId()))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(tc01, "TC-RES-01 must be present");
        Assert.assertEquals(tc01.getModule(), "Resource");
        
        TestCaseRow tc44 = rows.stream()
                .filter(r -> "TC-RES-44".equals(r.getTestCaseId()))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(tc44, "TC-RES-44 must be present");
        Assert.assertEquals(tc44.getModule(), "Resource");
    }

    @Test
    public void testReadRunnableResourcesSheet() {
        String path = "src/test/resources/manual-testcases-runnable.xlsx";
        List<TestCaseRow> rows = ExcelTestCaseReader.readAll(path);
        
        long resourceRows = rows.stream()
                .filter(r -> "Resource".equalsIgnoreCase(r.getModule()))
                .count();
        
        Assert.assertEquals(resourceRows, 44, "Expected exactly 44 Resource test cases in manual-testcases-runnable.xlsx");
    }
}
