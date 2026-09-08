package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the Resource Preview modal dialog.
 */
public class ResourcePreviewModal extends BasePage {

    private final By previewModal = By.xpath("//div[contains(@class,'modal-dialog')][.//*[normalize-space()='Preview']]");
    private final By previewTitle = By.xpath("//div[contains(@class,'modal-title') and normalize-space()='Preview']");
    private final By documentIframe = By.xpath("//iframe[@title='resource']");
    private final By videoPlayer = By.cssSelector("video, .react-player");
    private final By closeBtn = By.cssSelector(".modal-header .btn-close, .modal-header button[aria-label='Close']");

    public ResourcePreviewModal(WebDriver driver) {
        super(driver);
    }

    /** True if the Preview modal dialog is visible. */
    public boolean isPreviewOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(previewTitle)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** True if the PDF viewer iframe is rendered inside the preview modal. */
    public boolean isDocumentViewerDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(documentIframe)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** True if the video player is rendered inside the preview modal. */
    public boolean isVideoPlayerDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(videoPlayer)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Closes the preview modal using the close icon in the header. */
    public void closePreview() {
        click(closeBtn);
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(previewModal));
        } catch (Exception ignored) {}
    }
}
