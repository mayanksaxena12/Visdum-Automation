package Pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    protected void click(By locator) {
        System.out.println("  [STEP] 🖱️ Click -> " + locator);
        // AG-Grid and React re-render rows after searches/refreshes. If the element we found is
        // replaced before the click lands, find it again and retry instead of failing the test.
        for (int attempt = 1; ; attempt++) {
            try {
                firstClickable(locator).click();
                return;
            } catch (org.openqa.selenium.StaleElementReferenceException stale) {
                if (attempt >= 3) {
                    throw stale;
                }
                System.out.println("  [STEP] element was re-rendered, retrying click (" + attempt + ")");
            }
        }
    }

    protected void type(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        // Never print a password to the console / Maven log.
        boolean secret = "password".equalsIgnoreCase(element.getAttribute("type"))
                || locator.toString().toLowerCase().contains("password");
        System.out.println("  [STEP] ⌨️ Type '" + (secret ? "********" : value) + "' -> " + locator);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        element.clear();
        element.sendKeys(value);
    }

    /**
     * Adds a frame to the test's HTML replay. Called after every click/type/select so the replay
     * actually plays step by step (before, only the final "Test Ended" frame was recorded).
     * Disable with -Dvideo.steps=false to speed up long runs.
     */
    protected void recordStep(String label) {
        if (Boolean.parseBoolean(System.getProperty("video.steps", "true"))) {
            String text = label.length() > 140 ? label.substring(0, 140) + "…" : label;
            utilities.ScreenRecorderUtil.captureFrame(driver, text);
        }
    }

    protected void clearField(By locator) {
        System.out.println("  [STEP] ⌫ Clear field -> " + locator);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        element.clear();
    }

    protected String text(By locator) {
        return firstVisible(locator).getText();
    }

    /**
     * First element matching {@code locator} that is actually displayed. Pages often keep hidden
     * copies of the same thing (a closed menu, a hidden modal, a label with the same text), and
     * Selenium's visibilityOfElementLocated only looks at the FIRST match, so it timed out whenever
     * that first match was a hidden one.
     */
    protected WebElement firstVisible(By locator) {
        return wait.ignoring(org.openqa.selenium.StaleElementReferenceException.class).until(d -> {
            for (WebElement e : d.findElements(locator)) {
                try {
                    if (e.isDisplayed()) {
                        return e;
                    }
                } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                }
            }
            return null;
        });
    }

    /** First displayed AND enabled match (what elementToBeClickable checks, but over all matches). */
    protected WebElement firstClickable(By locator) {
        return wait.until(d -> {
            for (WebElement e : d.findElements(locator)) {
                if (e.isDisplayed() && e.isEnabled()) {
                    return e;
                }
            }
            return null;
        });
    }

    /** True if any element matching {@code locator} becomes visible within the default wait. */
    protected boolean anyVisible(By locator) {
        try {
            return firstVisible(locator) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * The clickable control of a react-select, found by the placeholder it shows (or its current
     * value). The form also has <label>s with the same text ("Select Manager"); clicking a label
     * does not open the dropdown, which is why some dropdown steps silently did nothing before.
     */
    protected By reactSelectControl(String placeholderOrValue) {
        String l = xpathLiteral(placeholderOrValue);
        return By.xpath("//*[contains(@class,'select__control')]"
                + "[.//*[(contains(@class,'select__placeholder') or contains(@class,'select__single-value')"
                + " or contains(@class,'select__multi-value__label')) and normalize-space()=" + l + "]]");
    }

    /** Clicks a react-select open and returns its (focused) search input. */
    protected WebElement openReactSelect(String placeholderOrValue) {
        click(reactSelectControl(placeholderOrValue));
        return driver.switchTo().activeElement();
    }

    /**
     * Selects an option from a react-select dropdown. react-select renders its placeholder text on a
     * <div> (not the input) and only exposes a typeahead input, so the reliable interaction is:
     * click the control (via its placeholder text) to focus the input, type the option, press Enter.
     */
    protected void selectReactOption(String placeholder, String option) {
        System.out.println("  [STEP] 🔽 Select Option '" + option + "' for Dropdown -> '" + placeholder + "'");
        WebElement input = openReactSelect(placeholder);
        input.sendKeys(option);
        input.sendKeys(Keys.ENTER);
    }

    /**
     * Opens a react-select control and confirms whichever option it highlights by default (its
     * first option) without needing to know the option's exact label. Used for filter fields whose
     * choices are fetched from the backend at runtime (e.g. CustomFilter's Operator/Value selects).
     */
    protected void selectFirstReactOption(String placeholder) {
        openReactSelect(placeholder);
        // Options may be fetched after opening; wait for a real one before choosing it.
        firstClickable(REACT_OPTION).click();
    }

    /**
     * An option in an open react-select menu. Plain options carry {@code select__option}; custom
     * option components (e.g. the Manager picker's name + email UserSelectOption) drop that class but
     * keep react-select's {@code react-select-N-option-M} id, so match either.
     */
    protected static final By REACT_OPTION = By.cssSelector(".select__menu .select__option, .select__menu [id*='-option-']");

    protected String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        return "concat('" + value.replace("'", "',\"'\",'") + "')";
    }

    /**
     * AG-Grid virtualizes columns that sit outside the visible horizontal viewport: a header/cell
     * for an off-screen column (e.g. Currency, Employee Id, Manager on the Users grid) simply does
     * not exist in the DOM until the grid's own horizontal scrollbar moves far enough to render it.
     * Locating such a column directly (without scrolling first) either times out or silently
     * targets nothing -- which is why the scrollbar visibly never moved during those runs.
     *
     * <p>This scrolls AG-Grid's actual virtualized render container ({@code .ag-center-cols-
     * viewport}) directly -- not the separate {@code .ag-body-horizontal-scroll-viewport} track
     * that's purely a visual scrollbar -- so AG-Grid recalculates its rendered column range as a
     * real browser {@code scroll} event fires automatically from the {@code scrollLeft} write.
     * Scrolls in small increments until the requested column's header appears, or gives up at the
     * end of the scroll range (e.g. for pinned columns like "Status"/"Actions" that never scroll).
     */
    protected void scrollColumnIntoView(String colId) {
        // The header can already be in the DOM (AG Grid renders a buffer of columns) while it is
        // still clipped off-screen, so wait for it to be *displayed*, not merely present.
        By header = By.xpath("//div[@role='columnheader' and @col-id='" + colId + "']");
        java.util.function.BooleanSupplier shown = () -> {
            try {
                return driver.findElements(header).stream().anyMatch(WebElement::isDisplayed);
            } catch (org.openqa.selenium.StaleElementReferenceException rerendered) {
                return false;
            }
        };
        if (shown.getAsBoolean()) {
            return;
        }

        // Scroll both the render viewport and the scrollbar track: depending on the AG Grid
        // version one of them drives the other (and the header) through its scroll event.
        List<WebElement> scrollers = driver.findElements(By.cssSelector(".ag-center-cols-viewport, .ag-body-horizontal-scroll-viewport"));
        if (scrollers.isEmpty()) {
            return; // Grid has no horizontal scrollbar (every column already fits on screen).
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long scrollWidth = 0;
        for (WebElement s : scrollers) {
            scrollWidth = Math.max(scrollWidth, ((Number) js.executeScript("return arguments[0].scrollWidth;", s)).longValue());
        }

        for (long position = 0; position <= scrollWidth + 250; position += 250) {
            for (WebElement s : scrollers) {
                js.executeScript("arguments[0].scrollLeft = arguments[1];", s, position); // fires 'scroll' itself
            }
            try {
                new WebDriverWait(driver, Duration.ofMillis(400)).until(d -> shown.getAsBoolean());
                return;
            } catch (org.openqa.selenium.TimeoutException notYet) {
                // keep scrolling right
            }
        }
    }
}
