package utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Centralized Excel Test Data Reader.
 * Primary source of truth for all test data across the automation framework.
 * Reads directly from src/test/resources/manual-testcases.xlsx (or override path).
 */
public final class ExcelTestDataReader {

    private static final String DEFAULT_WORKBOOK_PATH = "src/test/resources/manual-testcases.xlsx";

    private ExcelTestDataReader() {
    }

    /**
     * Retrieves the raw cell value for a specific TC ID and column name.
     */
    public static String getRawValue(String sheetName, String tcId, String targetColumn) {
        String path = System.getProperty("excel.file", DEFAULT_WORKBOOK_PATH);
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalStateException("Excel test cases workbook not found at: " + file.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                // Try case-insensitive lookup
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    if (workbook.getSheetName(i).equalsIgnoreCase(sheetName)) {
                        sheet = workbook.getSheetAt(i);
                        break;
                    }
                }
            }

            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in workbook: " + path);
            }

            DataFormatter formatter = new DataFormatter();
            int headerRowIdx = findHeaderRowIndex(sheet, formatter);
            Row headerRow = sheet.getRow(headerRowIdx);

            int tcIdCol = -1;
            int targetCol = -1;

            for (Cell cell : headerRow) {
                String val = formatter.formatCellValue(cell).trim();
                if ("Test Case ID".equalsIgnoreCase(val) || "TC ID".equalsIgnoreCase(val)) {
                    tcIdCol = cell.getColumnIndex();
                }
                if (val.equalsIgnoreCase(targetColumn)) {
                    targetCol = cell.getColumnIndex();
                }
            }

            if (tcIdCol == -1) {
                throw new IllegalStateException("Could not find 'Test Case ID' header in sheet: " + sheetName);
            }
            if (targetCol == -1) {
                throw new IllegalArgumentException("Could not find column '" + targetColumn + "' in sheet: " + sheetName);
            }

            for (int r = headerRowIdx + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String currentTcId = formatter.formatCellValue(row.getCell(tcIdCol)).trim();
                if (currentTcId.equalsIgnoreCase(tcId)) {
                    return formatter.formatCellValue(row.getCell(targetCol)).trim();
                }
            }

            throw new IllegalArgumentException("Test Case ID '" + tcId + "' not found in sheet: " + sheetName);

        } catch (Exception e) {
            throw new RuntimeException("Failed reading Excel test data for TC ID: " + tcId + " in sheet: " + sheetName, e);
        }
    }

    /**
     * Extracts and parses the 'Test Data' cell into a structured key-value map.
     * Supports formats like:
     *   Name: Test User
     *   Email: user@example.com
     *   Role: Individual Contributor
     *   Password: Password@123
     */
    public static Map<String, String> getTestDataMap(String sheetName, String tcId) {
        String testDataRaw = getRawValue(sheetName, tcId, "Test Data");
        return parseTestDataString(testDataRaw);
    }

    /**
     * Parses raw Test Data strings containing key-value pairs into a Map.
     */
    public static Map<String, String> parseTestDataString(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyMap();
        }

        Map<String, String> map = new LinkedHashMap<>();
        // Split by lines or semicolons
        String[] lines = raw.split("[\\r\\n;,]+");
        Pattern kvPattern = Pattern.compile("^\\s*([A-Za-z0-9_\\s]+)\\s*[:=]\\s*(.*)$");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            Matcher matcher = kvPattern.matcher(trimmed);
            if (matcher.matches()) {
                String key = matcher.group(1).trim();
                String value = matcher.group(2).trim();
                // Strip enclosing single or double quotes
                if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
                    value = value.substring(1, value.length() - 1).trim();
                }
                map.put(key, value);
            } else {
                // Store standalone values with an indexed key or default
                if (!map.containsKey("value")) {
                    map.put("value", trimmed);
                }
            }
        }

        return map;
    }

    /**
     * Helper to find header row in a sheet.
     */
    private static int findHeaderRowIndex(Sheet sheet, DataFormatter formatter) {
        for (int r = sheet.getFirstRowNum(); r <= Math.min(sheet.getFirstRowNum() + 10, sheet.getLastRowNum()); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (Cell cell : row) {
                String val = formatter.formatCellValue(cell).trim();
                if ("Test Case ID".equalsIgnoreCase(val) || "TC ID".equalsIgnoreCase(val)) {
                    return r;
                }
            }
        }
        return sheet.getFirstRowNum();
    }
}
