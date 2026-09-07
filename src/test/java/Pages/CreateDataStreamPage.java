package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Multi-step Create / Edit Data Stream wizard page object
 * (from data-streams/create-stream/_CreateStream.tsx and Stepper components).
 *
 * <p>Steps:
 * 1. Stream Details (Name, Type, Department, Data Source)
 * 2. Data Mapping
 * 3. Define Criteria
 * 4. Stream Scheduling
 * 5. Deal Credits
 * 6. Review & Save
 */
public class CreateDataStreamPage extends BasePage {

    // Step 1: Stream Details
    private final By streamNameInput = By.xpath("//input[@name='name' or @name='stream_name' or @placeholder='Enter Stream Name']");
    private final By connectedAppsSourceBtn = By.xpath("//button[contains(.,'Connected Apps')]");
    private final By uploadSheetsSourceBtn = By.xpath("//button[contains(.,'Upload Sheets')]");
    private final By baseViewsSourceBtn = By.xpath("//button[contains(.,'Base Views')]");
    private final By dealCreditsRadioYes = By.xpath("//label[contains(.,'automatically process deal credits')]/..//input[@type='radio' and @value='YES'] | //input[@type='radio' and @value='YES'][following-sibling::text()[contains(.,'Yes')]]");
    private final By dealCreditsRadioNo = By.xpath("//label[contains(.,'automatically process deal credits')]/..//input[@type='radio' and @value='NO'] | //input[@type='radio' and @value='NO'][following-sibling::text()[contains(.,'No')]]");
    private final By fetchNewRecordsRadioYes = By.xpath("//label[contains(.,'Fetch new records')]/..//input[@type='radio' and @value='YES']");
    private final By fetchNewRecordsRadioNo = By.xpath("//label[contains(.,'Fetch new records')]/..//input[@type='radio' and @value='NO']");

    // Stepper Footer Navigation
    private final By nextStepBtn = By.xpath("//button[normalize-space()='Next' or .//span[normalize-space()='Next']]");
    private final By submitBtn = By.xpath("//button[normalize-space()='Submit' or .//span[normalize-space()='Submit']]");
    private final By saveToDraftBtn = By.xpath("//button[normalize-space()='Save to draft' or .//span[normalize-space()='Save to draft']]");
    private final By previousStepBtn = By.xpath("//button[normalize-space()='Previous' or .//span[normalize-space()='Previous']]");
    private final By cancelBtn = By.xpath("//button[normalize-space()='Cancel' or .//span[normalize-space()='Cancel']]");

    public CreateDataStreamPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(streamNameInput)).isDisplayed()
                    || wait.until(ExpectedConditions.visibilityOfElementLocated(nextStepBtn)).isDisplayed();
        } catch (Exception e) {
            return driver.getCurrentUrl().contains("data-streams/add") || driver.getCurrentUrl().contains("data-streams/edit");
        }
    }

    public void enterStreamName(String streamName) {
        type(streamNameInput, streamName);
    }

    public String getStreamName() {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(streamNameInput));
        return input.getAttribute("value");
    }

    public boolean isStreamNameDisabled() {
        WebElement input = driver.findElement(streamNameInput);
        return input.getAttribute("disabled") != null;
    }

    public void selectStreamType(String streamType) {
        selectReactOption("Choose Stream Type", streamType);
    }

    public void selectDepartment(String department) {
        selectReactOption("Choose Department", department);
    }

    public void selectDataSource(String source) {
        if ("Connected Apps".equalsIgnoreCase(source)) {
            click(connectedAppsSourceBtn);
        } else if ("Upload Sheets".equalsIgnoreCase(source)) {
            click(uploadSheetsSourceBtn);
        } else if ("Base Views".equalsIgnoreCase(source)) {
            click(baseViewsSourceBtn);
        }
    }

    public void selectApp(String appName) {
        selectReactOption("Choose App", appName);
    }

    public void selectDataCategory(String category) {
        selectReactOption("Choose Data Category", category);
    }

    public void selectBaseView(String baseViewName) {
        selectReactOption("Choose Base View", baseViewName);
    }

    public void uploadManualSheet(String absoluteFilePath) {
        WebElement fileInput = driver.findElement(By.cssSelector("input[type='file']"));
        fileInput.sendKeys(absoluteFilePath);
    }

    public boolean isDataSourceSelectionVisible() {
        try {
            return !driver.findElements(connectedAppsSourceBtn).isEmpty() && driver.findElement(connectedAppsSourceBtn).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDropzoneVisible() {
        try {
            By locator = By.xpath("//*[contains(@class,'vis-file-dropzone') or contains(@class,'dropzone') or input[@type='file']]");
            return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBaseViewSelectVisible() {
        try {
            By locator = By.xpath("//*[normalize-space()='Choose Base View']");
            return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAppSelectVisible() {
        try {
            By locator = By.xpath("//*[normalize-space()='Choose App']");
            return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void enterStreamDetails(String streamName, String streamType, String department) {
        if (streamName != null && !streamName.isBlank()) {
            enterStreamName(streamName);
        }
        if (streamType != null && !streamType.isBlank()) {
            try {
                selectStreamType(streamType);
            } catch (Exception ignored) {
            }
        }
        if (department != null && !department.isBlank()) {
            try {
                selectDepartment(department);
            } catch (Exception ignored) {
            }
        }
    }

    public void setAutoProcessDealCredits(boolean enable) {
        click(enable ? dealCreditsRadioYes : dealCreditsRadioNo);
    }

    public boolean isDealCreditsRadioVisible() {
        return !driver.findElements(By.xpath("//label[contains(.,'deal credits')]")).isEmpty();
    }

    public void setFetchOnlyNewRecords(boolean enable) {
        click(enable ? fetchNewRecordsRadioYes : fetchNewRecordsRadioNo);
    }

    public void clickNextStep() {
        click(nextStepBtn);
    }

    public void clickSubmit() {
        click(submitBtn);
    }

    public void clickSaveAndActivate() {
        try {
            click(submitBtn);
        } catch (Exception e) {
            click(nextStepBtn);
        }
    }

    public void clickSaveToDraft() {
        click(saveToDraftBtn);
    }

    public void clickPreviousStep() {
        click(previousStepBtn);
    }

    public void clickCancel() {
        click(cancelBtn);
    }

    public boolean isSaveToDraftVisible() {
        return !driver.findElements(saveToDraftBtn).isEmpty();
    }

    public boolean isSubmitVisible() {
        return !driver.findElements(submitBtn).isEmpty();
    }

    public boolean isNextVisible() {
        return !driver.findElements(nextStepBtn).isEmpty();
    }

    public boolean isFieldErrorVisible(String text) {
        return !driver.findElements(By.xpath("//*[contains(@class,'fv-help-block') or contains(@class,'invalid-feedback') or contains(@class,'text-danger')][contains(text()," + xpathLiteral(text) + ")]")).isEmpty();
    }
}
