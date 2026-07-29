package utilities;

import java.util.Map;

/** Immutable row read from the data-driven control file (one test case). */
public class TestCaseRow {

    private final String testCaseId;
    private final String module;
    private final String scenario;
    private final String description;
    private final boolean run;
    private final Map<String, String> data;

    public TestCaseRow(String testCaseId, String module, String scenario, String description,
            boolean run, Map<String, String> data) {
        this.testCaseId = testCaseId;
        this.module = module;
        this.scenario = scenario;
        this.description = description;
        this.run = run;
        this.data = data;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public String getModule() {
        return module;
    }

    public String getScenario() {
        return scenario;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRun() {
        return run;
    }

    /** Dispatch key used by {@link TestCaseRegistry}: "Module:Scenario" (case-insensitive). */
    public String actionKey() {
        return (module + ":" + scenario).trim().toLowerCase();
    }

    /** Returns a named data column value (e.g. "Param1"), or empty string if absent. */
    public String get(String column) {
        String v = data.get(column);
        return v == null ? "" : v.trim();
    }

    public String param1() {
        return get("Param1");
    }

    public String param2() {
        return get("Param2");
    }

    @Override
    public String toString() {
        return testCaseId + " [" + module + ":" + scenario + "] " + description;
    }
}
