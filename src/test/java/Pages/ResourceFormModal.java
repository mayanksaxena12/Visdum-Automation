package Pages;

import java.io.File;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page object for the Add Resource and Update Resource modal (ResourceSettings.tsx).
 */
public class ResourceFormModal extends BasePage {

    private final By modalContainer = By.cssSelector(".modal.show, .modal-dialog");
    private final By modalTitle = By.xpath("//div[contains(@class,'modal') and contains(@class,'show')]//div[contains(@class,'modal-title')] | //div[contains(@class,'modal-title')]");
    private final By nameInput = By.name("name");
    private final By fileInput = By.xpath("//div[contains(@class,'dropzone-wrapper')]//input[@type='file'] | //input[@type='file']");
    private final By submitBtn = By.xpath("//div[contains(@class,'modal') and contains(@class,'show')]//div[contains(@class,'modal-footer')]//button[@type='submit'] | //div[contains(@class,'modal-footer')]//button[@type='submit']");
    private final By cancelBtn = By.xpath("//div[contains(@class,'modal') and contains(@class,'show')]//div[contains(@class,'modal-footer')]//button[normalize-space()='Cancel'] | //div[contains(@class,'modal-footer')]//button[normalize-space()='Cancel']");
    private final By deleteFileBtn = By.cssSelector(".deleteBtnClass, button[title='Delete']");
    private final By uploadedFileBox = By.cssSelector(".custom-box");
    private final By toastSuccess = By.xpath("//div[contains(@class,'Toastify__toast--success')] | //*[contains(text(),'Resource added successfully') or contains(text(),'Resource updated')]");

    public ResourceFormModal(WebDriver driver) {
        super(driver);
    }

    /** True once the modal dialog is rendered on the screen (name input is visible). */
    public boolean isModalOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** True once the modal dialog has closed (name input is invisible or unmounted). */
    public boolean isModalClosed() {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(nameInput));
        } catch (Exception e) {
            return true;
        }
    }

    /** Returns the modal title ("Add Resource" or "Update Resource"). */
    public String getModalTitle() {
        return text(modalTitle);
    }

    /** Enters the resource name in the name input field. */
    public void enterResourceName(String name) {
        type(nameInput, name);
    }

    /** Clears the resource name input field. */
    public void clearResourceName() {
        clearField(nameInput);
    }

    /** Returns the current value in the resource name input field. */
    public String getResourceNameValue() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput)).getAttribute("value");
    }

    /** Selects a role from the Roles react-select dropdown (e.g. "Manager", "Leader"). */
    public void selectRole(String roleName) {
        selectReactOption("Select roles", roleName);
    }

    /** Selects a department from the Departments react-select dropdown. */
    public void selectDepartment(String deptName) {
        selectReactOption("Select departments", deptName);
    }

    /** Selects a user from the Users react-select dropdown. */
    public void selectUser(String userName) {
        selectReactOption("Select users", userName);
    }

    /** Clicks 'Select All' inside the Roles dropdown. */
    public void selectAllRoles() {
        clickSelectAllFor("Select roles");
    }

    /** Clicks 'Select All' inside the Departments dropdown. */
    public void selectAllDepartments() {
        clickSelectAllFor("Select departments");
    }

    /** Clicks 'Select All' inside the Users dropdown. */
    public void selectAllUsers() {
        clickSelectAllFor("Select users");
    }

    private void clickSelectAllFor(String placeholder) {
        WebElement control = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//*[normalize-space()=" + xpathLiteral(placeholder) + "])[1]")));
        control.click();
        By selectAllCheckbox = By.xpath("//div[contains(@class,'custom-select-class')]//input[@type='checkbox']");
        click(selectAllCheckbox);
        // Press escape to close dropdown menu
        driver.switchTo().activeElement().sendKeys(org.openqa.selenium.Keys.ESCAPE);
    }

    /**
     * Uploads a file (PDF or MP4) by sending the absolute path to the Dropzone file input.
     */
    public void uploadFile(String absoluteFilePath) {
        File file = new File(absoluteFilePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File to upload does not exist: " + absoluteFilePath);
        }
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(fileInput));
        input.sendKeys(file.getAbsolutePath());
    }

    /** True if an uploaded file preview card is visible in the Dropzone. */
    public boolean isFileUploaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(uploadedFileBox)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Clicks the trash can icon to remove the uploaded file from the Dropzone. */
    public void removeUploadedFile() {
        click(deleteFileBtn);
    }

    /** Clicks the Add or Update submit button. */
    public void submit() {
        click(submitBtn);
    }

    /** Clicks the Cancel button. */
    public void cancel() {
        click(cancelBtn);
    }

    /** Returns the submit button text ("Add" or "Update"). */
    public String getSubmitButtonText() {
        return text(submitBtn).trim();
    }

    /** True if a field or group validation message matching {@code message} is visible. */
    public boolean isValidationMessageVisible(String message) {
        try {
            By locator = By.xpath("//span[@role='alert' and contains(normalize-space(.)," + xpathLiteral(message) + ")]"
                    + " | //div[contains(@class,'fv-help-block') and contains(normalize-space(.)," + xpathLiteral(message) + ")]"
                    + " | //*[contains(@class,'Toastify') and contains(normalize-space(.)," + xpathLiteral(message) + ")]");
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** True if a success toast notification appears after saving. */
    public boolean isSuccessToastDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(toastSuccess)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
}
