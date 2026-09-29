package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the 3-step Plan Assignment Wizard (/plans/assign-plan/add).
 * Covers Step 1 (Assign Individual), Step 2 (Program Details), Step 3 (Review & Submit),
 * and Stepper action buttons.
 */
public class CreateAssignPlanWizardPage extends BasePage {

    // Step 1: Dropdown Placeholders & Date Inputs
    private final String participantPlaceholder = "Select Participant";
    private final String planTemplatePlaceholder = "Select Plan Template";
    private final By startDateInput = By.xpath("//input[contains(@placeholder,'Start Date') or contains(@class,'flatpickr-input')][1]");
    private final By endDateInput = By.xpath("//input[contains(@placeholder,'End Date')][1]");
    private final By validationErrors = By.xpath("//div[contains(@class,'text-danger')]");

    // Stepper Footer Controls
    private final By nextBtn = By.xpath("//button[normalize-space()='Next' or (contains(@class,'btn-primary') and contains(.,'Next'))]");
    private final By saveToDraftBtn = By.xpath("//button[normalize-space()='Save to draft' or contains(.,'Save to draft')]");
    private final By cancelBtn = By.xpath("//button[normalize-space()='Cancel' or contains(.,'Cancel')]");
    private final By submitBtn = By.xpath("//button[normalize-space()='Submit' or (contains(@class,'btn-primary') and contains(.,'Submit'))]");

    public CreateAssignPlanWizardPage(WebDriver driver) {
        super(driver);
    }

    public void selectParticipant(String participantName) {
        selectReactOption(participantPlaceholder, participantName);
    }

    public void selectPlanTemplate(String templateName) {
        selectReactOption(planTemplatePlaceholder, templateName);
    }

    public void enterStartDate(String dateStr) {
        if (!driver.findElements(startDateInput).isEmpty()) {
            type(startDateInput, dateStr);
        }
    }

    public void enterEndDate(String dateStr) {
        if (!driver.findElements(endDateInput).isEmpty()) {
            type(endDateInput, dateStr);
        }
    }

    public void clickNext() {
        click(nextBtn);
    }

    public void clickSaveToDraft() {
        click(saveToDraftBtn);
    }

    public void clickCancel() {
        click(cancelBtn);
    }

    public void clickSubmit() {
        click(submitBtn);
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.urlContains("/plans/assign-plan/add"));
    }

    public List<String> getValidationErrors() {
        return driver.findElements(validationErrors).stream()
                .map(WebElement::getText)
                .filter(t -> !t.trim().isEmpty())
                .toList();
    }

    public boolean hasValidationError(String expectedText) {
        return getValidationErrors().stream().anyMatch(e -> e.contains(expectedText));
    }
}
