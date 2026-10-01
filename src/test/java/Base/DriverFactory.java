package Base;

import java.time.Duration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import utilities.ConfigReader;

/**
 * Thread-safe WebDriver provider.
 *
 * <p>Backed by a {@link ThreadLocal} so that parallel TestNG threads each get their own isolated
 * ChromeDriver instance -- essential now that the suite runs in parallel. The previous single
 * {@code static WebDriver} field would have been shared across threads and corrupted state.
 *
 * <p>WebDriverManager still resolves the matching driver binary automatically. The browser is
 * launched maximized with a bounded page-load timeout to avoid indefinite hangs on slow pages.
 * Supports headless execution via `-Dheadless=true` or `headless=true` in `config.properties`.
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

            String sysHeadless = System.getProperty("headless");
            String configHeadless = ConfigReader.get("headless", "false");
            boolean isHeadless = "true".equalsIgnoreCase(sysHeadless) || "true".equalsIgnoreCase(configHeadless);

            if (isHeadless) {
                System.out.println("[BROWSER] Starting Chrome in HEADLESS mode (background, no UI)...");
                options.addArguments("--headless=new");
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
            } else {
                System.out.println("[BROWSER] Starting Chrome in NORMAL mode (visible UI window)...");
                options.addArguments("--start-maximized");
            }

            options.addArguments("--remote-allow-origins=*");

            java.util.Map<String, Object> prefs = new java.util.HashMap<>();
            java.io.File downloadDir = new java.io.File("test-output/downloads");
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            prefs.put("download.default_directory", downloadDir.getAbsolutePath());
            prefs.put("download.prompt_for_download", false);
            prefs.put("download.directory_upgrade", true);
            prefs.put("safebrowsing.enabled", true);
            options.setExperimentalOption("prefs", prefs);

            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

            if (isHeadless) {
                driver.manage().window().setSize(new Dimension(1920, 1080));
            }

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
