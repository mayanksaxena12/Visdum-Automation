package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the Delete Resource confirmation modal (ResourceActionCell.tsx).
 */
public class ResourceDeleteModal extends BasePage {

    private final By confirmationText = By.xpath("//*[contains(text(),'Are you sure you want to delete this resource?')]");
    private final By cancelBtn = By.xpath("//div[contains(@class,'modal') and contains(@class,'show')][.//*[contains(text(),'Are you sure you want to delete')]]//button[normalize-space()='Cancel']"
            + " | //div[contains(@class,'modal-dialog')][.//*[contains(text(),'Are you sure you want to delete')]]//button[normalize-space()='Cancel']");
    private final By submitBtn = By.xpath("//div[contains(@class,'modal') and contains(@class,'show')][.//*[contains(text(),'Are you sure you want to delete')]]//button[normalize-space()='Submit']"
            + " | //div[contains(@class,'modal-dialog')][.//*[contains(text(),'Are you sure you want to delete')]]//button[normalize-space()='Submit']");
    private final By deleteToast = By.xpath("//*[contains(text(),'Resource Deleted Successfully')]");

    public ResourceDeleteModal(WebDriver driver) {
        super(driver);
    }

    /** True if the delete confirmation dialog is visible. */
    public boolean isDeleteModalOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(confirmationText)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Clicks Cancel to dismiss the delete modal without deleting. */
    public void cancelDelete() {
        click(cancelBtn);
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmationText));
        } catch (Exception ignored) {}
    }

    /** Clicks Submit to confirm deletion of the resource. */
    public void confirmDelete() {
        click(submitBtn);
    }

    /** True if the 'Resource Deleted Successfully' toast notification appears. */
    public boolean isDeleteSuccessToastDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(deleteToast)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
}
