package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Create/Edit team drawer from TeamActionModals/TeamForm.tsx. */
public class TeamFormPage extends BasePage {

    private final By name = By.name("name");
    private final By description = By.name("description");
    private final By createButton = By.xpath("//button[normalize-space()='Create']");
    private final By saveButton = By.xpath("//button[normalize-space()='Save']");
    private final By cancelButton = By.xpath("//button[normalize-space()='Cancel']");

    public TeamFormPage(WebDriver driver) {
        super(driver);
    }

    public void enterTeamName(String teamName) {
        type(name, teamName);
    }

    public void enterTeamDescription(String teamDescription) {
        type(description, teamDescription);
    }

    /** Submits the drawer when creating a brand new team (button reads "Create"). */
    public void submitNewTeam() {
        click(createButton);
    }

    /** Submits the drawer when editing an existing team (button reads "Save"). */
    public void saveEditedTeam() {
        click(saveButton);
    }

    public void cancel() {
        click(cancelButton);
    }

    /** editTeamSchema in TeamForm.tsx only validates the Yup "required" message on name. */
    public boolean isValidationMessageVisible(String message) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@role='alert' and normalize-space()=" + xpathLiteral(message) + "]"))).isDisplayed();
    }
}
