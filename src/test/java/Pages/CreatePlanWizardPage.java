package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the 4-step Advance Plan Creation Wizard (/plans/create-plan/add).
 * Covers Step 1 (Plan Details), Step 2 (Add Components), Step 3 (Create Rule with Expression Builder),
 * Step 4 (Review & Submit), and Stepper footer actions (Next, Save as Draft, Cancel).
 */
public class CreatePlanWizardPage extends BasePage {

    // Step 1: Plan Details Locators
    private final By planNameInput = By.name("name");
    private final By descriptionInput = By.name("description");
    private final String payoutPeriodPlaceholder = "Select Payout Period";
    private final String departmentPlaceholder = "Select Department";
    private final By validationErrors = By.xpath("//div[contains(@class,'text-danger')]");

    // Stepper Footer Action Buttons
    private final By nextBtn = By.xpath("//button[normalize-space()='Next' or (contains(@class,'btn-primary') and contains(.,'Next'))]");
    private final By saveToDraftBtn = By.xpath("//button[normalize-space()='Save to draft' or contains(.,'Save to draft')]");
    private final By cancelBtn = By.xpath("//button[normalize-space()='Cancel' or contains(.,'Cancel')]");
    private final By submitBtn = By.xpath("//button[normalize-space()='Submit' or (contains(@class,'btn-primary') and contains(.,'Submit'))]");

    // Stepper Indicators
    private final By activeStepperItem = By.xpath("//div[contains(@class,'stepper-item') and (contains(@class,'current') or contains(@class,'active'))]");

    // Step 2: Add Components Locators
    private final By componentNameInput = By.xpath("//input[contains(@placeholder,'Component Name') or @name='component_name']");
    private final String earningStreamPlaceholder = "Select Earning Data Stream";

    // Step 3: Expression Builder Helper
    private final ExpressionBuilderHelper expressionBuilder;

    public CreatePlanWizardPage(WebDriver driver) {
        super(driver);
        this.expressionBuilder = new ExpressionBuilderHelper(driver);
    }

    public ExpressionBuilderHelper expressionBuilder() {
        return expressionBuilder;
    }

    // --- Step 1 Actions ---

    public void enterPlanName(String name) {
        type(planNameInput, name);
    }

    public void selectPayoutPeriod(String period) {
        selectReactOption(payoutPeriodPlaceholder, period);
    }

    public void selectDepartment(String department) {
        selectReactOption(departmentPlaceholder, department);
    }

    public void enterDescription(String desc) {
        if (!driver.findElements(descriptionInput).isEmpty()) {
            type(descriptionInput, desc);
        }
    }

    // --- Stepper Footer Actions ---

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

    // --- Validations & State Checkers ---

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(planNameInput)).isDisplayed();
    }

    /** Returns all validation error messages visible on the current step */
    public List<String> getValidationErrors() {
        return driver.findElements(validationErrors).stream()
                .map(WebElement::getText)
                .filter(txt -> !txt.trim().isEmpty())
                .toList();
    }

    /** Returns true if a specific validation error text is displayed */
    public boolean hasValidationError(String expectedText) {
        return getValidationErrors().stream().anyMatch(e -> e.contains(expectedText));
    }
}
