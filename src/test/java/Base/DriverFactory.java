package Base;

import java.time.Duration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Thread-safe WebDriver provider.
 *
 * <p>Backed by a {@link ThreadLocal} so that parallel TestNG threads each get their own isolated
 * ChromeDriver instance -- essential now that the suite runs in parallel. The previous single
 * {@code static WebDriver} field would have been shared across threads and corrupted state.
 *
 * <p>WebDriverManager still resolves the matching driver binary automatically. The browser is
 * launched maximized with a bounded page-load timeout to avoid indefinite hangs on slow pages.
 */
public class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    /** Returns the current thread's driver, creating one on first use. */
    public static WebDriver getDriver() {
        if (DRIVER.get() == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized", "--remote-allow-origins=*");
            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            DRIVER.set(driver);
        }
        return DRIVER.get();
    }

    /** True if the current thread already has a live driver (does NOT create one). */
    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    /** Quits and clears the current thread's driver, if any. Safe to call unconditionally. */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
