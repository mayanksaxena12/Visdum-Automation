package Base;

import java.time.Duration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import utilities.ConfigReader;

/**
 * Thread-safe WebDriver provider.
 *
 * <p>Backed by a {@link ThreadLocal} so that parallel TestNG threads each get their own isolated
 * browser. WebDriverManager resolves the matching driver binary automatically.
 *
 * <p><b>Browser choice:</b> {@code -Dbrowser=chrome|edge} or {@code browser=} in config.properties
 * (default chrome). Headless via {@code -Dheadless=true} or {@code headless=true}.
 *
 * <p><b>Windows Smart App Control / Application Control:</b> every Chrome update makes Selenium
 * download a brand-new, unsigned chromedriver.exe. Windows can block it ("An Application Control
 * policy has blocked this file", CreateProcess error=4551), so no browser starts and every test
 * fails. When that happens this factory switches to Microsoft Edge (same Chromium engine, and its
 * driver is signed by Microsoft) for the rest of the run. If no browser can start at all, the
 * remaining tests fail immediately with one clear message instead of each retrying.
 */
public class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    /** Browser that actually started (after any fallback); reused for the rest of the run. */
    private static volatile String activeBrowser;
    /** Set when browser start-up is blocked by Windows policy: later tests fail fast with it. */
    private static volatile RuntimeException launchBlocked;

    private DriverFactory() {
    }

    /** Returns the current thread's driver, creating one on first use. */
    public static WebDriver getDriver() {
        if (DRIVER.get() == null) {
            if (launchBlocked != null) {
                throw new IllegalStateException(launchBlocked.getMessage(), launchBlocked);
            }
            boolean headless = isHeadless();
            String browser = activeBrowser != null ? activeBrowser : requestedBrowser();
            WebDriver driver;
            try {
                driver = start(browser, headless);
            } catch (RuntimeException first) {
                if (!isBlockedByWindowsPolicy(first)) {
                    throw first;
                }
                if (!"chrome".equals(browser)) {
                    launchBlocked = new IllegalStateException(blockedMessage(browser, first), first);
                    throw launchBlocked;
                }
                System.out.println("[BROWSER] Windows blocked the Chrome driver (" + rootMessage(first)
                        + "). Switching to Microsoft Edge for this run.");
                try {
                    driver = start("edge", headless);
                    browser = "edge";
                } catch (RuntimeException second) {
                    String why = isBlockedByWindowsPolicy(second)
                            ? blockedMessage("chrome and edge", second)
                            : "Windows blocked the Chrome driver, and Edge could not start either: " + rootMessage(second);
                    launchBlocked = new IllegalStateException(why, second);
                    throw launchBlocked;
                }
            }
            activeBrowser = browser;

            // Record a replay frame for every click / type / navigation / JS click / Actions call,
            // no matter which class performs it (see utilities.StepRecorderListener).
            WebDriver recorded = new org.openqa.selenium.support.events.EventFiringDecorator<>(
                    new utilities.StepRecorderListener(driver)).decorate(driver);

            DRIVER.set(recorded);
        }
        return DRIVER.get();
    }

    private static WebDriver start(String browser, boolean headless) {
        WebDriver driver;
        if ("edge".equals(browser)) {
            // WebDriverManager 5.8 can no longer download the Edge driver (Microsoft retired its old
            // hosts), so fetch it from msedgedriver.microsoft.com ourselves (cached per version).
            EdgeDriverResolver.resolve();
            EdgeOptions options = new EdgeOptions();
            configure(options, headless, "Edge");
            driver = new EdgeDriver(options);
        } else {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            configure(options, headless, "Chrome");
            driver = new ChromeDriver(options);
        }
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        if (headless) {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        } else {
            try {
                driver.manage().window().maximize();
            } catch (Exception ignored) {
            }
        }
        return driver;
    }

    /** Same options for Chrome and Edge (both Chromium). */
    private static void configure(ChromiumOptions<?> options, boolean headless, String label) {
        if (headless) {
            System.out.println("[BROWSER] Starting " + label + " in HEADLESS mode (background, no UI)...");
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        } else {
            System.out.println("[BROWSER] Starting " + label + " in NORMAL mode (visible UI window)...");
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
    }

    private static boolean isHeadless() {
        String sysHeadless = System.getProperty("headless");
        if (sysHeadless != null && !sysHeadless.isBlank()) {
            return Boolean.parseBoolean(sysHeadless);
        }
        return Boolean.parseBoolean(ConfigReader.get("headless", "false"));
    }

    private static String requestedBrowser() {
        String b = System.getProperty("browser");
        if (b == null || b.isBlank()) {
            b = ConfigReader.get("browser", "chrome");
        }
        b = b.trim().toLowerCase();
        return b.contains("edge") ? "edge" : "chrome";
    }

    /** True when Windows refused to run the driver executable (Smart App Control / WDAC / AppLocker). */
    static boolean isBlockedByWindowsPolicy(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            String m = c.getMessage();
            if (m != null && (m.contains("Application Control policy") || m.contains("error=4551")
                    || m.contains("blocked by group policy") || m.contains("error=1260"))) {
                return true;
            }
        }
        return false;
    }

    private static String rootMessage(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) {
            c = c.getCause();
        }
        return c.getMessage();
    }

    private static String blockedMessage(String browsers, Throwable t) {
        return "No browser could start: Windows blocked the " + browsers + " driver (" + rootMessage(t) + "). "
                + "This is Windows Smart App Control / Application Control, not a test failure. "
                + "Run with -Dbrowser=edge, or allow the driver in Windows Security > App & browser control.";
    }

    /** Name of the browser actually in use ("chrome" / "edge"), or null before the first start. */
    public static String activeBrowser() {
        return activeBrowser;
    }

    /** True if the current thread already has a live driver (does NOT create one). */
    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    /** Quits and clears the current thread's driver, if any. Safe to call unconditionally. */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }
}
