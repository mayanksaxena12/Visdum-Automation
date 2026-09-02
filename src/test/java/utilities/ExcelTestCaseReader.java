package utilities;

import java.io.FileInputStream;
import java.io.InputStream;
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
 * Reads the QA team's manual Excel test-case workbook and turns every sheet into executable
 * {@link TestCaseRow}s.
 *
 * <p>Handles the real workbook layout: a title/metadata band at the top, a header row located by
 * finding the "Test Case ID" column (row 3 on Login/Users, row 2 on Team/Departments), section
 * divider rows, and columns that appear in a slightly different order per sheet (mapped by header
 * NAME, so order does not matter).
 *
 * <p>Each sheet is mapped to a module ({@link #SHEET_MODULE}) and each row's free-text
 * "Test Scenario" is classified into an automation scenario token ({@link #classify}). The
 * resulting {@code Module:Scenario} key is what {@link TestCaseRegistry} dispatches on. Rows whose
 * scenario has no registered automation (or belong to the Login module, which is manual) are still
 * read and returned -- the runner reports them as skipped/manual rather than dropping them, so no
 * sheet or row is lost during configuration.
 */
public final class ExcelTestCaseReader {

    /** Sheet name -> automation module. Every sheet in the workbook is mapped here. */
    public static final Map<String, String> SHEET_MODULE = new LinkedHashMap<>();
    static {
        SHEET_MODULE.put("Login", "Login");        // manual (login/OTP/SSO not auto-driven)
        SHEET_MODULE.put("Users", "User");
        SHEET_MODULE.put("Team", "Team");
        SHEET_MODULE.put("Departments", "Department");
        SHEET_MODULE.put("Data Streams", "DataStream");
        SHEET_MODULE.put("DataStreams", "DataStream");
        SHEET_MODULE.put("Data Stream", "DataStream");
    }

    /** Scenario tokens that have safe, read-only automation in TestCaseRegistry. */
    private static final List<String> AUTO_SCENARIOS = List.of(
            "search", "sort", "columnfilter", "view",
            "validlogin", "invalidpassword", "invalidemail", "emptyfields",
            // "emailonly", "passwordonly", "emailformat", "emaillength", "passwordlength");
            "emailonly", "passwordonly", "emailformat", "emaillength", "passwordlength",
            "otpsent", "otp", "wrongotp", "emptyotp", "shortotp", "alphaotp",
            "pasteotp", "otpformat");

    private ExcelTestCaseReader() {
    }

    public static List<TestCaseRow> readAll(String path) {
        List<TestCaseRow> rows = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();
        try (InputStream in = new FileInputStream(path); Workbook wb = new XSSFWorkbook(in)) {
            for (Sheet sheet : wb) {
                String sheetName = sheet.getSheetName();
                String module = SHEET_MODULE.getOrDefault(sheetName, sheetName);
                int headerRowIdx = findHeaderRow(sheet, fmt);
                if (headerRowIdx < 0) {
                    continue;
                }
                Map<String, Integer> cols = headerIndex(sheet.getRow(headerRowIdx), fmt);
                Integer tcCol = cols.get("Test Case ID");
                if (tcCol == null) {
                    continue;
                }
                for (int r = headerRowIdx + 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) {
                        continue;
                    }
                    String tcId = cellVal(fmt, row, tcCol);
                    if (tcId.isEmpty() || tcId.equalsIgnoreCase("Test Case ID")) {
                        continue; // divider / metadata / blank row
                    }
                    String scenarioText = colVal(fmt, row, cols, "Test Scenario");
                    String description = colVal(fmt, row, cols, "Test Case Description");
                    String testData = colVal(fmt, row, cols, "Test Data");
                    String runRaw = colVal(fmt, row, cols, "Run"); // optional column

                    String scenarioToken = classify(module, scenarioText, description);
                    boolean run = resolveRun(runRaw, module, scenarioToken);

                    Map<String, String> data = new LinkedHashMap<>();
                    data.put("Sheet", sheetName);
                    data.put("TestScenarioText", scenarioText);
                    data.put("TestData", testData);
                    data.put("Param1", defaultParam(module, scenarioToken, testData));

                    rows.add(new TestCaseRow(tcId, module, scenarioToken, description, run, data));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel workbook: " + path, e);
        }
        return rows;
    }

    public static List<TestCaseRow> readRunnable(String path) {
        List<TestCaseRow> out = new ArrayList<>();
        for (TestCaseRow row : readAll(path)) {
            if (row.isRun()) {
                out.add(row);
            }
        }
        return out;
    }

    /** Classifies free-text scenario/description into an automation scenario token (module-aware). */
    public static String classify(String module, String scenario, String description) {
        String t = (scenario + " " + description).toLowerCase();
        if ("Login".equalsIgnoreCase(module)) {
            // OTP / 2-step verification flow (OTP read live from the UAT database).
            if (t.contains("otp") || t.contains("2-step") || t.contains("two step")
                    || t.contains("two-factor") || t.contains("two factor")) {
                if (t.contains("is sent") || t.contains("sent after")
                        || t.contains("redirect to otp screen")) {
                    return "otpsent";
                }
                if (t.contains("wrong otp") || (t.contains("wrong") && t.contains("otp"))) {
                    return "wrongotp";
                }
                if (t.contains("empty otp") || t.contains("without entering otp")
                        || (t.contains("empty") && t.contains("otp"))
                        || (t.contains("blank") && t.contains("otp"))) {
                    return "emptyotp";
                }
                if (t.contains("incomplete") || t.contains("fewer than") || t.contains("partial")
                        || (t.contains("less than") && t.contains("digit"))) {
                    return "shortotp";
                }
                if (t.contains("alphab") || t.contains("special character")
                        || t.contains("combination of valid and invalid")) {
                    return "alphaotp";
                }
                if (t.contains("past")) {
                    return "pasteotp";
                }
                if (t.contains("correct otp") || t.contains("login success")
                        || t.contains("verified successfully")) {
                    return "otp";
                }
                if (t.contains("6-digit") || t.contains("six digit") || t.contains("numeric")) {
                    return "otpformat";
                }
                return "manual"; // resend / timer / expired / delivery / reuse / session / network
            }

            if (t.contains("valid login") || (t.contains("login") && t.contains("valid email and password"))) {
                return "validlogin";
            }
            if (t.contains("incorrect password") || t.contains("invalid password")
                    || t.contains("wrong password")) {
                return "invalidpassword";
            }
            if (t.contains("unregistered") || t.contains("invalid email") || t.contains("unregistered email")) {
                return "invalidemail";
            }
            if (t.contains("blank email & password") || t.contains("blank email and password")
                    || (t.contains("empty") && t.contains("blank"))) {
                return "emptyfields";
            }
            if (t.contains("only email")) return "emailonly";
            if (t.contains("only password")) return "passwordonly";
            if (t.contains("email format") || t.contains("invalid email format")) return "emailformat";
            if (t.contains("more than 50") || t.contains("maximum 50") || t.contains("50 char")) {
                return "emaillength";
            }
            if (t.contains("less than 8") || t.contains("minimum 8") || t.contains("8 char")) {
                return "passwordlength";
            }
            return "manual"; // OTP / 2FA / resend / timer / forgot-password / SSO / profile
        }
        if (t.contains("add member")) return "addmembers";
        if (t.contains("create") || t.contains("add new")) return "create";
        if (t.contains("edit")) return "edit";
        if (t.contains("activate") && !t.contains("deactivate")) return "activate";
        if (t.contains("deactivate")) return "deactivate";
        if (t.contains("custom filter")) return "customfilter";
        if (t.contains("sort")) return "sort";
        if (t.contains("filter")) return "columnfilter";
        if (t.contains("search")) return "search";
        if (t.contains("view")) return "view";
        return "manual";
    }

    public static boolean isAutoExecutable(TestCaseRow row) {
        return TestCaseRegistry.isMapped(row.actionKey());
    }

    /** Prints a per-sheet coverage summary (total / auto-executable / manual). */
    public static String coverageSummary(String path) {
        List<TestCaseRow> all = readAll(path);
        Map<String, int[]> perSheet = new LinkedHashMap<>();
        for (TestCaseRow row : all) {
            String sheet = row.get("Sheet");
            int[] counts = perSheet.computeIfAbsent(sheet, k -> new int[2]);
            counts[0]++;
            if (isAutoExecutable(row)) {
                counts[1]++;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== Excel Coverage Summary ===\n");
        for (Map.Entry<String, int[]> e : perSheet.entrySet()) {
            String module = SHEET_MODULE.getOrDefault(e.getKey(), e.getKey());
            int total = e.getValue()[0];
            int auto = e.getValue()[1];
            sb.append(String.format("  Sheet '%s' -> module '%s': %d rows, %d auto-executable, %d manual%n",
                    e.getKey(), module, total, auto, total - auto));
        }
        return sb.toString();
    }

    // ---------- helpers ----------

    private static int findHeaderRow(Sheet sheet, DataFormatter fmt) {
        for (int r = sheet.getFirstRowNum(); r <= Math.min(sheet.getFirstRowNum() + 6, sheet.getLastRowNum()); r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            for (Cell cell : row) {
                if ("Test Case ID".equalsIgnoreCase(fmt.formatCellValue(cell).trim())) {
                    return r;
                }
            }
        }
        return -1;
    }

    private static Map<String, Integer> headerIndex(Row headerRow, DataFormatter fmt) {
        Map<String, Integer> cols = new LinkedHashMap<>();
        for (Cell cell : headerRow) {
            String name = fmt.formatCellValue(cell).trim();
            if (!name.isEmpty()) {
                cols.put(name, cell.getColumnIndex());
            }
        }
        return cols;
    }

    private static String colVal(DataFormatter fmt, Row row, Map<String, Integer> cols, String name) {
        Integer idx = cols.get(name);
        return idx == null ? "" : cellVal(fmt, row, idx);
    }

    private static String cellVal(DataFormatter fmt, Row row, int col) {
        return fmt.formatCellValue(row.getCell(col)).trim();
    }

    private static boolean resolveRun(String runRaw, String module, String scenarioToken) {
        String r = runRaw.trim().toLowerCase();
        if (!r.isEmpty()) {
            return r.equals("yes") || r.equals("y") || r.equals("true") || r.equals("1");
        }
        // No explicit Run column: default to running any row that has a registered automation.
        String key = (module + ":" + scenarioToken).trim().toLowerCase();
        return TestCaseRegistry.isMapped(key);
    }

    private static String defaultParam(String module, String scenarioToken, String testData) {
        if (scenarioToken.equals("sort") || scenarioToken.equals("columnfilter")) {
            return "name"; // primary sortable/filterable column in every module grid
        }
        // search / view need a lookup term
        switch (module) {
            case "User": return System.getProperty("excel.user.search", "Mayank");
            case "Team": return System.getProperty("excel.team.search", "Sales");
            case "Department": return System.getProperty("excel.department.search", "Sales");
            default: return "";
        }
    }
}
