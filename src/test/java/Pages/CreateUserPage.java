package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Three-step user drawer from UserActionModals/UserForm.tsx. */
public class CreateUserPage extends BasePage {

    private final By name = By.name("name");
    private final By email = By.name("email");
    private final By userReferenceId = By.name("user_reference_id");
    private final By employeeId = By.name("employee_number");
    private final By password = By.name("password");
    private final By confirmPassword = By.name("confirm_password");
    // Step 1 uses a stepper button, step 2 uses a CustomButton (no data-dc-stepper-action attribute),
    // but both are labelled "Next Step" — match on the visible text so a single locator covers both.
    private final By nextStep = By.xpath("//button[normalize-space()='Next Step']");
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

    public void enterOfficialDetails(String employeeNumber, String manager, String team,
            String department) {
        type(employeeId, employeeNumber);
        selectOptional("Select Manager", manager);
        selectOptional("Select Team", team);
        selectOptional("Select Department", department);
    }

    public void enterPasswordDetails(String newPassword) {
        type(password, newPassword);
        type(confirmPassword, newPassword);
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

    private void selectOptional(String placeholder, String option) {
        if (option != null && !option.isBlank()) {
            selectReactOption(placeholder, option);
        }
    }
}
