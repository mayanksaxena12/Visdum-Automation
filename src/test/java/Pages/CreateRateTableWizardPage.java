package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utilities.ScreenRecorderUtil;

import java.util.List;

/**
 * Page object for the 4-step Create / Edit Rate Table Wizard
 * (/rate-table/add and /rate-table/edit).
 *
 * <p>Backed by frontend component {@code _CreateRateTable.tsx} and its Stepper subcomponents:
 * 1. Step 1: {@code 1_RateTableDetails.tsx} (Table name, manual file dropzone)
 * 2. Step 2: {@code 2_DataMapping.tsx} (Column & field mappings)
 * 3. Step 3: {@code 3_LookupRule.tsx} (Lookup rule configuration)
 * 4. Step 4: {@code 4_Review.tsx} (Review and submit)
 */
public class CreateRateTableWizardPage extends BasePage {

    // Step 1: Table Details Locators
    private final By tableNameInput = By.xpath("//input[@name='name' or @placeholder='Enter Table Name']");
    private final By fileDropzone = By.xpath("//div[contains(@class,'vis-file-dropzone')] | //div[contains(@class,'dropzone')]");
    private final By fileInput = By.xpath("//input[@type='file']");
    private final By validationErrors = By.xpath("//div[contains(@class,'fv-help-block') or contains(@class,'text-danger')]");

    // Stepper Navigation Footer Buttons
    private final By nextBtn = By.xpath("//button[normalize-space()='Next' or (contains(@class,'btn-primary') and contains(.,'Next'))]");
    private final By saveToDraftBtn = By.xpath("//button[normalize-space()='Save to draft' or contains(.,'Save to draft')]");
    private final By cancelBtn = By.xpath("//button[normalize-space()='Cancel' or contains(.,'Cancel')]");
    private final By previousBtn = By.xpath("//button[normalize-space()='Previous' or contains(.,'Previous')]");
    private final By submitBtn = By.xpath("//button[normalize-space()='Submit' or (contains(@class,'btn-primary') and contains(.,'Submit'))]");

    public CreateRateTableWizardPage(WebDriver driver) {
        super(driver);
    }

    /** Returns true once the Create / Edit Rate Table wizard has rendered. */
    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(tableNameInput)).isDisplayed();
        } catch (Exception e) {
            return driver.getCurrentUrl().contains("/rate-table/add") || driver.getCurrentUrl().contains("/rate-table/edit");
        }
    }

    // --- Step 1 Actions ---

    /** Types the rate table name. */
    public void enterTableName(String name) {
        type(tableNameInput, name);
    }

    /** Reads the currently typed rate table name. */
    public String getTableName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(tableNameInput)).getAttribute("value");
    }

    /** Uploads a file (CSV / XLSX) via the hidden file input. */
    public void uploadFile(String absoluteFilePath) {
        if (!driver.findElements(fileInput).isEmpty()) {
            driver.findElement(fileInput).sendKeys(absoluteFilePath);
            ScreenRecorderUtil.captureFrame(driver, "Uploaded file: " + absoluteFilePath);
        } else {
            click(fileDropzone);
        }
    }

    // --- Stepper Navigation Actions ---

    public void clickNext() {
        click(nextBtn);
    }

    public void clickSaveToDraft() {
        click(saveToDraftBtn);
    }

    public void clickCancel() {
        click(cancelBtn);
    }

    public void clickPrevious() {
        click(previousBtn);
    }

    public void clickSubmit() {
        click(submitBtn);
    }

    // --- Validations ---

    public List<String> getValidationErrors() {
        return driver.findElements(validationErrors).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(txt -> !txt.isEmpty())
                .toList();
    }

    public boolean hasValidationError(String expectedText) {
        return getValidationErrors().stream().anyMatch(e -> e.contains(expectedText));
    }
}
