package utilities;

import org.openqa.selenium.WebDriver;

/** A single executable automation action, driven by a {@link TestCaseRow}'s data. */
@FunctionalInterface
public interface TestAction {
    void run(WebDriver driver, TestCaseRow row) throws Exception;
}
