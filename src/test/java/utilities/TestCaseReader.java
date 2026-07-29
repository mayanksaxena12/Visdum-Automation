package utilities;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Reads the data-driven control file and returns test-case rows. Keeps test data fully external to
 * test logic: adding, editing, or toggling test cases is a file edit -- no code change required.
 *
 * <p>Supports both {@code .csv} and {@code .xlsx} (via Apache POI, already a project dependency).
 * Columns are mapped by header NAME (not position), so new columns can be added freely. Required
 * headers: {@code TestCaseId} and {@code Run}. Recommended: {@code Module}, {@code Scenario},
 * {@code Description}, {@code Param1}, {@code Param2}. Any additional columns are preserved and
 * accessible via {@link TestCaseRow#get(String)}.
 *
 * <p>A row executes only when its {@code Run} value is Yes/Y/True/1 (case-insensitive).
 */
public final class TestCaseReader {

    private TestCaseReader() {
    }

    public static List<TestCaseRow> readRunnable(String path) {
        List<TestCaseRow> all = readAll(path);
        List<TestCaseRow> runnable = new ArrayList<>();
        for (TestCaseRow row : all) {
            if (row.isRun()) {
                runnable.add(row);
            }
        }
        return runnable;
    }

    public static List<TestCaseRow> readAll(String path) {
        try {
            if (path.toLowerCase().endsWith(".xlsx")) {
                return readXlsx(path);
            }
            return readCsv(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read test-case control file: " + path, e);
        }
    }

    private static List<TestCaseRow> readCsv(String path) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(path));
        List<TestCaseRow> rows = new ArrayList<>();
        if (lines.isEmpty()) {
            return rows;
        }
        String[] headers = splitCsv(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            String[] cells = splitCsv(line);
            Map<String, String> map = new LinkedHashMap<>();
            for (int c = 0; c < headers.length; c++) {
                map.put(headers[c].trim(), c < cells.length ? cells[c] : "");
            }
            rows.add(toRow(map));
        }
        return rows;
    }

    private static List<TestCaseRow> readXlsx(String path) throws Exception {
        List<TestCaseRow> rows = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();
        try (InputStream in = new FileInputStream(path); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return rows;
            }
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(fmt.formatCellValue(cell).trim());
            }
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> map = new LinkedHashMap<>();
                boolean anyValue = false;
                for (int c = 0; c < headers.size(); c++) {
                    String v = fmt.formatCellValue(row.getCell(c));
                    if (!v.isEmpty()) {
                        anyValue = true;
                    }
                    map.put(headers.get(c), v);
                }
                if (anyValue) {
                    rows.add(toRow(map));
                }
            }
        }
        return rows;
    }

    private static TestCaseRow toRow(Map<String, String> map) {
        String id = map.getOrDefault("TestCaseId", "").trim();
        String module = map.getOrDefault("Module", "").trim();
        String scenario = map.getOrDefault("Scenario", "").trim();
        String description = map.getOrDefault("Description", "").trim();
        String runRaw = map.getOrDefault("Run", "").trim().toLowerCase();
        boolean run = runRaw.equals("yes") || runRaw.equals("y") || runRaw.equals("true")
                || runRaw.equals("1");
        return new TestCaseRow(id, module, scenario, description, run, map);
    }

    /** Minimal CSV splitter with double-quote support (handles commas inside quoted fields). */
    private static String[] splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
