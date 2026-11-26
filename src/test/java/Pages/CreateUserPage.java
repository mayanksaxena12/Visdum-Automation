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

    /**
     * Fills Step 1 (Personal Details) with valid values but leaves one field empty, so the only
     * reason Next Step can be blocked is that field. {@code skipField}: name, email, reference,
     * role or currency.
     */
    public void fillPersonalDetailsExcept(String skipField, String fullName, String emailAddress,
            String referenceId, String role, String currency) {
        if (!"name".equals(skipField)) {
            type(name, fullName);
        }
        if (!"email".equals(skipField)) {
            type(email, emailAddress);
        }
        if (!"reference".equals(skipField)) {
            type(userReferenceId, referenceId);
        }
        if (!"role".equals(skipField)) {
            selectReactOption("Select Role", role);
        }
        if (!"currency".equals(skipField)) {
            selectReactOption("Select Currency", currency);
        }
        recordStep("Step 1 filled, '" + skipField + "' left empty");
    }

    /** Waits for a validation message; returns false instead of throwing, and records the result. */
    public boolean waitForValidationMessage(String message) {
        boolean shown;
        try {
            shown = isValidationMessageVisible(message);
        } catch (Exception e) {
            shown = false;
        }
        recordStep("Verify message '" + message + "' shown: " + shown);
        return shown;
    }

    // ---------------- helpers for the Users-sheet ID mappings (TS_11_*, CS1-*, EU-*) ----------------

    /** Types {@code text} into a text input by its {@code name} attribute (name, email, user_reference_id, ...). */
    public void typeInto(String inputName, String text) {
        type(By.name(inputName), text);
    }

    /** Current value of a text input by its {@code name} attribute. */
    public String inputValue(String inputName) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.name(inputName))).getAttribute("value");
    }

    public boolean isInputEnabled(String inputName) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.name(inputName))).isEnabled();
    }

    /**
     * Types free text into a react-select (found by its placeholder), presses Enter and Escape.
     * Used to check that a dropdown does not accept a value that is not one of its options.
     */
    public void typeIntoDropdown(String placeholder, String text) {
        WebElement input = openReactSelect(placeholder);
        input.sendKeys(text);
        input.sendKeys(Keys.ENTER);
        input.sendKeys(Keys.ESCAPE);
        recordStep("Typed free text into '" + placeholder + "'");
    }

    /** True while the dropdown still shows its placeholder, i.e. no value is selected. */
    public boolean isDropdownEmpty(String placeholder) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'select__placeholder') and normalize-space()=" + xpathLiteral(placeholder) + "]")))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Opens a react-select by placeholder (or current value) and returns the option texts, then closes it. */
    public java.util.List<String> dropdownOptions(String placeholderOrValue) {
        openReactSelect(placeholderOrValue);
        java.util.List<String> options = wait.until(d -> {
            java.util.List<WebElement> els = d.findElements(By.cssSelector(".select__menu .select__option"));
            return els.isEmpty() ? null : els;
        }).stream().map(e -> e.getText().trim()).toList();
        recordStep("Opened dropdown '" + placeholderOrValue + "' (" + options.size() + " options)");
        driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
        return options;
    }

    /** Picks the first option of a react-select found by placeholder. */
    public void selectFirstOption(String placeholder) {
        // Options such as Manager are fetched from the server after the dropdown opens; pressing
        // Enter before they arrive selects nothing. Wait for a real option and click it.
        openReactSelect(placeholder);
        WebElement first = firstClickable(By.cssSelector(".select__menu .select__option"));
        String picked = first.getText();
        first.click();
        recordStep("Selected first option '" + picked + "' in '" + placeholder + "'");
    }

    public void enterJoiningDate(String isoDate) {
        setDateField(joiningDate, isoDate);
    }

    public String joiningDateValue() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(joiningDate)).getAttribute("value");
    }

    public String managerEffectiveDateValue() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(managerEffectiveDate)).getAttribute("value");
    }

    public void updateDesignation(String text) {
        type(designation, text);
    }

    /** Enters password + confirm on Step 3 and clicks Submit, without waiting for the drawer to close. */
    public void tryPasswordSubmit(String pwd, String confirm) {
        if (pwd != null) {
            type(password, pwd);
        }
        if (confirm != null) {
            type(confirmPassword, confirm);
        }
        click(submit);
    }

    /** Title of the open drawer, e.g. "Create New User" / "Edit User". */
    public boolean isDrawerTitle(String title) {
        return isTextVisible(title);
    }

    /** Clicks the drawer's close (X) button. */
    public void closeDrawer() {
        By closeBtn = By.xpath("//*[substring(@id, string-length(@id) - 5) = '_close']");
        WebElement visible = wait.until(d -> d.findElements(closeBtn).stream().filter(WebElement::isDisplayed).findFirst().orElse(null));
        visible.click();
    }

    /** True once the user form has closed (Name input gone). */
    public boolean isClosed() {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(name));
        } catch (Exception e) {
            return false;
        }
    }

    /** Uploads a file to the Profile Image drop zone; returns true if the preview image changed. */
    public boolean uploadProfileImage(java.io.File image) {
        By preview = By.cssSelector("img[alt='upload img']");
        String before = wait.until(ExpectedConditions.presenceOfElementLocated(preview)).getAttribute("src");
        WebElement fileInput = driver.findElements(By.cssSelector("input[type='file']")).get(0);
        fileInput.sendKeys(image.getAbsolutePath());
        try {
            return wait.until(d -> {
                String now = d.findElement(preview).getAttribute("src");
                return now != null && !now.equals(before);
            });
        } catch (Exception e) {
            return false;
        } finally {
            recordStep("Uploaded profile image " + image.getName());
        }
    }

    /** True if a form input with this {@code name} attribute is visible (e.g. "employee_number"). */
    public boolean isInputVisible(String inputName) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(inputName))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** True if text is visible anywhere in the open drawer/page (exact text of an element). */
    public boolean isTextVisible(String text) {
        return anyVisible(By.xpath("//*[normalize-space()=" + xpathLiteral(text) + "]"));
    }

    public boolean isAutoGeneratePasswordVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(autoGeneratePasswordBtn)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String passwordFieldType() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(password)).getAttribute("type");
    }

    public void typePassword(String value) {
        type(password, value);
    }

    public String passwordValue() {
        return driver.findElement(password).getAttribute("value");
    }

    public String confirmPasswordValue() {
        return driver.findElement(confirmPassword).getAttribute("value");
    }

    /** True while the drawer is still on Step 1 (the Name input is visible). */
    public boolean isOnPersonalDetailsStep() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(name)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
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
        wait.until(ExpectedConditions.invisibilityOfElementLocated(submit));
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
        wait.until(ExpectedConditions.invisibilityOfElementLocated(saveUser));
    }

    public boolean isValidationMessageVisible(String message) {
        String cleanMessage = message.endsWith(".") ? message.substring(0, message.length() - 1) : message;
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@role='alert' and contains(normalize-space(), " + xpathLiteral(cleanMessage) + ")]"))).isDisplayed();
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

    // ---- helpers for CS3-008 / CS3-009 / CS2-009 / CS3-012 ----
    public boolean isSendAlertChecked() {
        return firstVisible(sendAlertEmailCheckbox).isSelected();
    }

    public void toggleSendAlert() {
        firstVisible(sendAlertEmailCheckbox).click();
        recordStep("Toggled 'Send alert email'");
    }

    public void clickPrevious() {
        click(previousStep);
    }

}

