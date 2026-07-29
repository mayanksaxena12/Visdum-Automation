package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Create/Edit department drawer from DepartmentActionModals/DepartmentForm.tsx (mirrors TeamFormPage). */
public class DepartmentFormPage extends BasePage {

    private final By name = By.name("name");
    private final By description = By.name("description");
    private final By createButton = By.xpath("//button[normalize-space()='Create']");
    private final By saveButton = By.xpath("//button[normalize-space()='Save']");
    private final By cancelButton = By.xpath("//button[normalize-space()='Cancel']");

    public DepartmentFormPage(WebDriver driver) {
        super(driver);
    }

    public void enterDepartmentName(String departmentName) {
        type(name, departmentName);
    }

    public void enterDepartmentDescription(String departmentDescription) {
        type(description, departmentDescription);
    }

    public void submitNewDepartment() {
        click(createButton);
    }

    public void saveEditedDepartment() {
        click(saveButton);
    }

    public void cancel() {
        click(cancelButton);
    }

    /** editDepartmentSchema validates: required + min 3 + max 50 chars on Department Name. */
    public boolean isValidationMessageVisible(String message) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@role='alert' and normalize-space()=" + xpathLiteral(message) + "]"))).isDisplayed();
    }
}
