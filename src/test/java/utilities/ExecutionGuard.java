package utilities;

import org.testng.SkipException;

/** Prevents data-changing UAT tests from running unless explicitly enabled. */
public final class ExecutionGuard {

    private ExecutionGuard() {
    }

    public static void requireDestructiveTestsEnabled() {
        if (!Boolean.parseBoolean(System.getProperty("run.destructive.tests", "false"))) {
            throw new SkipException("Set -Drun.destructive.tests=true to run data-changing user tests.");
        }
    }
}
