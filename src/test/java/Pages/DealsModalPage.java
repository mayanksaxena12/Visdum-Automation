package Pages;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page object for the DealsModal component (DealsModal.tsx).
 * Handles the async progress tracking, in-progress message, progress bar,
 * completion metrics (Derived/Lookup columns processed, Records processed/updated),
 * and modal close/dismissal.
 */
public class DealsModalPage extends BasePage {

    private final By modalDialog = By.xpath("//div[contains(@class,'modal-dialog')]");
    private final By modalHeaderCloseBtn = By.xpath("//div[contains(@class,'modal-header')]//button[contains(@class,'btn-close') or @aria-label='Close']");
    private final By initialNoticeText = By.xpath("//div[contains(@class,'modal-body')]//span[contains(normalize-space(),'close this window')]");
    private final By progressBar = By.cssSelector("progress#file");

    // Completion metrics locators (specific to Refresh Columns non-glue path)
    private final By derivedColumnsProcessedSpan = By.xpath("//span[contains(normalize-space(),'Derived Columns Processed:')]");
    private final By lookupColumnsProcessedSpan = By.xpath("//span[contains(normalize-space(),'Lookup Columns Processed:')]");
    private final By recordsProcessedSpan = By.xpath("//span[contains(normalize-space(),'Records Processed:')]");
    private final By recordsUpdatedSpan = By.xpath("//span[contains(normalize-space(),'Records Updated:')]");

    // Generic completion / failure locators
    private final By completionSuccessMarker = By.xpath("//span[contains(@class,'bullet-dot') and contains(@class,'bg-success')]");
    private final By failureReasonMarker = By.xpath("//span[contains(@class,'bullet-dot') and contains(@class,'bg-danger')]/parent::*");

    public DealsModalPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Checks if the DealsModal is currently visible on the screen.
     */
    public boolean isOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(modalDialog)).isDisplayed();
        } catch (Exception e) {
            return !driver.findElements(modalDialog).isEmpty() && driver.findElement(modalDialog).isDisplayed();
        }
    }

    /**
     * Checks if the DealsModal has been closed / dismissed.
     */
    public boolean isClosed() {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(modalDialog));
        } catch (Exception e) {
            return driver.findElements(modalDialog).isEmpty();
        }
    }

    /**
     * Checks if the in-progress informational notice is displayed:
     * "Don't worry, you can close this window and get things done in the meantime".
     */
    public boolean isInitialNoticeDisplayed() {
        try {
            return !driver.findElements(initialNoticeText).isEmpty()
                    && driver.findElement(initialNoticeText).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the progress bar element is present and visible.
     */
    public boolean isProgressBarPresent() {
        try {
            return !driver.findElements(progressBar).isEmpty()
                    && driver.findElement(progressBar).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the current progress percentage value from the progress bar.
     */
    public int getProgressPercentage() {
        try {
            WebElement bar = driver.findElement(progressBar);
            String val = bar.getAttribute("value");
            return val != null && !val.isBlank() ? Integer.parseInt(val.trim()) : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Waits for the async Refresh Columns job to finish (either COMPLETED or FAILED).
     *
     * @param timeoutSeconds maximum seconds to wait
     * @return true if completed or failed within the timeout, false otherwise
     */
    public boolean waitForCompletion(int timeoutSeconds) {
        WebDriverWait completionWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        try {
            return completionWait.until(d -> isCompleted() || isFailed());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the modal has reached a COMPLETED state displaying metrics.
     */
    public boolean isCompleted() {
        try {
            return !driver.findElements(derivedColumnsProcessedSpan).isEmpty()
                    || !driver.findElements(recordsProcessedSpan).isEmpty()
                    || (!driver.findElements(completionSuccessMarker).isEmpty()
                            && driver.findElements(progressBar).isEmpty());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the modal has surfaced a FAILED state.
     */
    public boolean isFailed() {
        try {
            return !driver.findElements(failureReasonMarker).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the error reason text if the processing job failed.
     */
    public String getFailureReason() {
        try {
            if (isFailed()) {
                return text(failureReasonMarker).trim();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * Extracts the integer count of Derived Columns Processed.
     */
    public int getDerivedColumnsProcessedCount() {
        return extractCountFromMetric(derivedColumnsProcessedSpan);
    }

    /**
     * Extracts the integer count of Lookup Columns Processed.
     */
    public int getLookupColumnsProcessedCount() {
        return extractCountFromMetric(lookupColumnsProcessedSpan);
    }

    /**
     * Extracts the integer count of Records Processed.
     */
    public int getRecordsProcessedCount() {
        return extractCountFromMetric(recordsProcessedSpan);
    }

    /**
     * Extracts the integer count of Records Updated.
     */
    public int getRecordsUpdatedCount() {
        return extractCountFromMetric(recordsUpdatedSpan);
    }

    /**
     * Closes the DealsModal by clicking the header close button (X).
     */
    public void closeModal() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(modalHeaderCloseBtn)).click();
        } catch (Exception e) {
            click(modalHeaderCloseBtn);
        }
    }

    private int extractCountFromMetric(By locator) {
        try {
            WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            String fullText = elem.getText().trim();
            // Regex to match the first number after the colon
            Matcher matcher = Pattern.compile(":\\s*([0-9,]+)").matcher(fullText);
            if (matcher.find()) {
                String numStr = matcher.group(1).replace(",", "");
                return Integer.parseInt(numStr);
            }
        } catch (Exception e) {
            System.err.println("Could not parse metric for locator " + locator + ": " + e.getMessage());
        }
        return -1;
    }
}
