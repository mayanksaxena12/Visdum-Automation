package utilities;

import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.events.WebDriverListener;

/**
 * Records a replay frame for EVERY browser action a test performs, whichever class performs it:
 * page objects, tests, registry lambdas, direct {@code driver.findElement(...).click()}, JavaScript
 * clicks and Actions (right-click / hover / drag). Installed by {@code DriverFactory} through
 * Selenium's {@code EventFiringDecorator}, so nothing has to remember to call captureFrame.
 *
 * <ul>
 *   <li>Before a click the target is outlined in red and captured, so the replay shows exactly
 *       what is about to be clicked.</li>
 *   <li>After typing, clearing, navigating, a JS click or an Actions sequence, the result is
 *       captured.</li>
 *   <li>Typed values are never put in frame labels; password fields are labelled as such.</li>
 * </ul>
 *
 * Screenshots are taken from the raw (undecorated) driver so recording never triggers itself.
 * Disable with {@code -Dvideo.steps=false}.
 */
public class StepRecorderListener implements WebDriverListener {

    private final WebDriver raw;

    public StepRecorderListener(WebDriver raw) {
        this.raw = raw;
    }

    private static boolean enabled() {
        return Boolean.parseBoolean(System.getProperty("video.steps", "true"));
    }

    private void frame(String label) {
        if (enabled()) {
            ScreenRecorderUtil.captureFrame(raw, label.length() > 160 ? label.substring(0, 160) + "…" : label);
        }
    }

    /** Short human description of an element: visible text, else name/placeholder/id, else tag. */
    private static String describe(WebElement el) {
        try {
            String text = el.getText();
            if (text != null && !text.isBlank()) {
                text = text.trim().replaceAll("\\s+", " ");
                return "'" + (text.length() > 60 ? text.substring(0, 60) + "…" : text) + "'";
            }
            for (String attr : new String[] {"aria-label", "name", "placeholder", "id", "title"}) {
                String v = el.getAttribute(attr);
                if (v != null && !v.isBlank()) {
                    return el.getTagName() + "[" + attr + "=" + v + "]";
                }
            }
            return el.getTagName();
        } catch (Exception e) {
            return "element";
        }
    }

    private static boolean isPassword(WebElement el) {
        try {
            String type = el.getAttribute("type");
            String name = el.getAttribute("name");
            return "password".equalsIgnoreCase(type) || (name != null && name.toLowerCase().contains("password"));
        } catch (Exception e) {
            return false;
        }
    }

    private void outline(WebElement el, boolean on) {
        try {
            ((JavascriptExecutor) raw).executeScript(on
                    ? "arguments[0].setAttribute('data-rec-outline', arguments[0].style.outline || '');"
                      + "arguments[0].style.outline='3px solid #ef4444';arguments[0].style.outlineOffset='2px';"
                    : "if (arguments[0].hasAttribute('data-rec-outline')) {"
                      + "arguments[0].style.outline=arguments[0].getAttribute('data-rec-outline');"
                      + "arguments[0].style.outlineOffset='';arguments[0].removeAttribute('data-rec-outline');}", el);
        } catch (Exception ignored) {
            // element may have been re-rendered or detached; recording must never break a test
        }
    }

    @Override
    public void beforeClick(WebElement element) {
        if (!enabled()) {
            return;
        }
        String what = describe(element);
        outline(element, true);
        frame("Click " + what);
        outline(element, false);
    }

    @Override
    public void afterSendKeys(WebElement element, CharSequence... keysToSend) {
        String what = describe(element);
        frame(isPassword(element) ? "Type password into " + what : "Type into " + what);
    }

    @Override
    public void afterClear(WebElement element) {
        frame("Clear " + describe(element));
    }

    @Override
    public void afterGet(WebDriver driver, String url) {
        frame("Open " + url);
    }

    @Override
    public void afterTo(WebDriver.Navigation navigation, String url) {
        frame("Open " + url);
    }

    @Override
    public void afterBack(WebDriver.Navigation navigation) {
        frame("Browser back");
    }

    @Override
    public void afterRefresh(WebDriver.Navigation navigation) {
        frame("Page refresh");
    }

    @Override
    public void afterExecuteScript(WebDriver driver, String script, Object[] args, Object result) {
        // Only scripts that act on the page (JS clicks, value changes); not read-only queries.
        String s = script == null ? "" : script;
        if (s.contains(".click()") || s.contains("dispatchEvent") || s.contains(".value =") || s.contains(".value=")) {
            frame("Script action: " + s.replaceAll("\\s+", " "));
        }
    }

    @Override
    public void afterPerform(WebDriver driver, java.util.Collection<Sequence> actions) {
        frame("Mouse/keyboard action");
    }

    @Override
    public void afterWindow(WebDriver.TargetLocator targetLocator, String nameOrHandle, WebDriver driver) {
        frame("Switch to window " + nameOrHandle);
    }

    @Override
    public void afterFindElements(WebDriver driver, org.openqa.selenium.By locator, List<WebElement> result) {
        // intentionally not recorded: lookups are not user actions and run in tight wait loops
    }
}
