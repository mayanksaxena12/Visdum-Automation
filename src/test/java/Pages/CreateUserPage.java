package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Three-step user drawer from UserActionModals/UserForm.tsx. */
public class CreateUserPage extends BasePage {

    // Step 1: Personal Details
    private final By name = By.name("name");
    private final By email = By.name("email");
    private final By userReferenceId = By.name("user_reference_id");

    // Step 2: Official Details
    private final By employeeId = By.name("employee_number");
    private final By joiningDate = By.cssSelector("input[name='date_of_joining']");
    private final By designation = By.name("designation");
    private final By bankInfo = By.name("bank_info");
    private final By taxInfo = By.name("tax_info");
    private final By managerEffectiveDate = By.cssSelector("input[name='manager_effective_date']");

    // Step 3: Password Details
    private final By password = By.name("password");
    private final By confirmPassword = By.name("confirm_password");
    private final By autoGeneratePasswordBtn = By.xpath("//button[normalize-space()='Auto - Generate Password']");
    private final By sendAlertEmailCheckbox = By.name("send_alert_email");

    // Stepper & Action Buttons
    private final By nextStep = By.xpath("//button[normalize-space()='Next Step']");
    private final By previousStep = By.xpath("//button[normalize-space()='Previous']");
    private final By saveUser = By.xpath("//button[normalize-space()='Save User']");
    private final By submit = By.xpath("//button[normalize-space()='Submit']");

    public CreateUserPage(WebDriver driver) {
        super(driver);
    }

    public void enterPersonalDetails(String fullName, String emailAddress, String referenceId,
            String role, String currency) {
        type(name, fullName);
        type(email, emailAddress);
        type(userReferenceId, referenceId);
        selectReactOption("Select Role", role);
        selectReactOption("Select Currency", currency);
    }

    public void clickNextStep() {
        click(nextStep);
    }

    public void clickPreviousStep() {
        click(previousStep);
    }

    /** Legacy enterOfficialDetails for basic fields. */
    public void enterOfficialDetails(String employeeNumber, String manager, String team,
            String department) {
        type(employeeId, employeeNumber);
        selectOptional("Select Manager", manager);
        selectOptional("Select Team", team);
        selectOptional("Select Department", department);
    }

    /** Extended enterOfficialDetails including joining date, designation, manager effective date, bank info, and tax info. */
    public void enterOfficialDetails(String employeeNumber, String joiningDateIso, String designationText,
            String manager, String managerEffectiveDateIso, String team, String department,
            String bankDetails, String taxDetails) {
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            type(employeeId, employeeNumber);
        }
        if (joiningDateIso != null && !joiningDateIso.isBlank()) {
            setDateField(joiningDate, joiningDateIso);
        }
        if (designationText != null && !designationText.isBlank()) {
            type(designation, designationText);
        }
        if (bankDetails != null && !bankDetails.isBlank()) {
            type(bankInfo, bankDetails);
        }
        if (taxDetails != null && !taxDetails.isBlank()) {
            type(taxInfo, taxDetails);
        }
        selectOptional("Select Manager", manager);
        if (manager != null && !manager.isBlank() && managerEffectiveDateIso != null && !managerEffectiveDateIso.isBlank()) {
            setDateField(managerEffectiveDate, managerEffectiveDateIso);
        }
        selectOptional("Select Team", team);
        selectOptional("Select Department", department);
    }

    public void enterPasswordDetails(String newPassword) {
        type(password, newPassword);
        type(confirmPassword, newPassword);
    }

    public void enterPasswordDetails(String newPassword, boolean sendAlertEmail) {
        type(password, newPassword);
        type(confirmPassword, newPassword);
        setSendAlertEmail(sendAlertEmail);
    }

    public void clickAutoGeneratePassword() {
        click(autoGeneratePasswordBtn);
    }

    public void setSendAlertEmail(boolean check) {
        WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(sendAlertEmailCheckbox));
        if (checkbox.isSelected() != check) {
            checkbox.click();
        }
    }

    public void submitNewUser() {
        click(submit);
    }

    public void enterName(String fullName) {
        type(name, fullName);
    }

    public void enterEmail(String emailAddress) {
        type(email, emailAddress);
    }

    public void updateName(String updatedName) {
        type(name, updatedName);
    }

    public void saveEditedUser() {
        click(saveUser);
    }

    public boolean isValidationMessageVisible(String message) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@role='alert' and normalize-space()=" + xpathLiteral(message) + "]"))).isDisplayed();
    }

    private void setDateField(By locator, String isoDate) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        input.clear();
        input.sendKeys(isoDate);
        input.sendKeys(Keys.ENTER);
    }

    private void selectOptional(String placeholder, String option) {
        if (option != null && !option.isBlank()) {
            selectReactOption(placeholder, option);
        }
    }
}

