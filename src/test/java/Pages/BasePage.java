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
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(value);
    }

    protected String text(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    /**
     * Selects an option from a react-select dropdown. react-select renders its placeholder text on a
     * <div> (not the input) and only exposes a typeahead input, so the reliable interaction is:
     * click the control (via its placeholder text) to focus the input, type the option, press Enter.
     */
    protected void selectReactOption(String placeholder, String option) {
        WebElement control = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//*[normalize-space()=" + xpathLiteral(placeholder) + "])[1]")));
        control.click();
        WebElement input = driver.switchTo().activeElement();
        input.sendKeys(option);
        input.sendKeys(Keys.ENTER);
    }

    /**
     * Opens a react-select control and confirms whichever option it highlights by default (its
     * first option) without needing to know the option's exact label. Used for filter fields whose
     * choices are fetched from the backend at runtime (e.g. CustomFilter's Operator/Value selects).
     */
    protected void selectFirstReactOption(String placeholder) {
        WebElement control = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//*[normalize-space()=" + xpathLiteral(placeholder) + "])[1]")));
        control.click();
        driver.switchTo().activeElement().sendKeys(Keys.ENTER);
    }

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
        By columnLocator = By.xpath("//div[@col-id='" + colId + "']");
        if (!driver.findElements(columnLocator).isEmpty()) {
            return;
        }

        List<WebElement> scrollContainers = driver.findElements(By.cssSelector(".ag-center-cols-viewport"));
        if (scrollContainers.isEmpty()) {
            return; // Grid has no horizontal scrollbar (every column already fits on screen).
        }

        WebElement viewport = scrollContainers.get(0);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long scrollWidth = ((Number) js.executeScript("return arguments[0].scrollWidth;", viewport)).longValue();
        long step = 250;

        for (long position = 0; position <= scrollWidth; position += step) {
            js.executeScript("arguments[0].scrollLeft = arguments[1];", viewport, position);
            if (!driver.findElements(columnLocator).isEmpty()) {
                return;
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Last resort: jump straight to the far right edge in case the column sits in the final
        // buffered range that a fixed-size step could have stepped over.
        js.executeScript("arguments[0].scrollLeft = arguments[0].scrollWidth;", viewport);
    }
}
