package utilities;

import org.testng.SkipException;

/** Prevents data-changing UAT tests from running unless explicitly enabled. */
public final class ExecutionGuard {

    private ExecutionGuard() {
    }

    public static boolean isDestructiveTestsEnabled() {
        return Boolean.parseBoolean(System.getProperty("run.destructive.tests", "false"));
    }

    public static void requireDestructiveTestsEnabled() {
        if (!isDestructiveTestsEnabled()) {
            throw new SkipException("Set -Drun.destructive.tests=true to run data-changing user tests.");
        }
    }
}
