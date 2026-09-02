package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * 6-step Create Data Stream wizard page object (from data-streams/create-stream/Stepper).
 *
 * <p>Steps:
 * 1. Stream Details
 * 2. Data Mapping
 * 3. Define Criteria
 * 4. Stream Scheduling
 * 5. Deal Credits
 * 6. Review & Save
 */
public class CreateDataStreamPage extends BasePage {

    // Step 1: Stream Details
    private final By streamNameInput = By.xpath("//input[@name='name' or @name='stream_name' or @placeholder='Enter Stream Name']");
    private final By platformSelect = By.xpath("//*[normalize-space()='Select Platform']");
    private final By objectEntitySelect = By.xpath("//*[normalize-space()='Select Object']");

    // Stepper Navigation
    private final By nextStepBtn = By.xpath("//button[normalize-space()='Next' or normalize-space()='Next Step' or normalize-space()='Submit']");
    private final By previousStepBtn = By.xpath("//button[normalize-space()='Previous']");
    private final By saveAndActivateBtn = By.xpath("//button[contains(normalize-space(),'Save') or contains(normalize-space(),'Activate') or contains(normalize-space(),'Submit')]");

    public CreateDataStreamPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(nextStepBtn)).isDisplayed();
    }

    public void enterStreamDetails(String streamName, String streamType, String department) {
        if (streamName != null && !streamName.isBlank()) {
            type(streamNameInput, streamName);
        }
        if (streamType != null && !streamType.isBlank()) {
            try {
                selectReactOption("Choose Stream Type", streamType);
            } catch (Exception e) {
                try {
                    selectReactOption("Choose App", streamType);
                } catch (Exception ignored) {
                }
            }
        }
        if (department != null && !department.isBlank()) {
            try {
                selectReactOption("Choose Department", department);
            } catch (Exception ignored) {
            }
        }
    }

    public void clickNextStep() {
        click(nextStepBtn);
    }

    public void clickPreviousStep() {
        click(previousStepBtn);
    }

    public void clickSaveAndActivate() {
        click(saveAndActivateBtn);
    }

    public boolean isFieldErrorVisible(String text) {
        return !driver.findElements(By.xpath("//*[contains(@class,'fv-help-block') or contains(@class,'invalid-feedback') or contains(@class,'text-danger')][contains(text()," + xpathLiteral(text) + ")]")).isEmpty();
    }
}
