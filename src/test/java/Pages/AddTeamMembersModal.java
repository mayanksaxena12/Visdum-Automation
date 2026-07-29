package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * "Add Members" modal opened from a team's row-action dropdown (TeamActionsCell.tsx dispatches
 * {@code setUserAssignModal} + {@code setActiveStepper(1)}), rendered by
 * components/UserAssignModal.tsx.
 *
 * <p>Users already on the target team render greyed-out and non-selectable
 * ({@code isRowSelectable} in UserAssignModal.tsx checks {@code team !== value?.name}), so this
 * only ever targets a row that AG-Grid actually allows selecting. The submit button reads "Add"
 * because UserAssignModal passes {@code buttonName='Add'} to the shared StepActionFooter, and its
 * final-step button label is {@code buttonName || 'Submit'} (see _StepActionFooter.tsx).
 */
public class AddTeamMembersModal extends BasePage {

    private final By searchBox = By.cssSelector("input[placeholder='Search']");
    private final By addButton = By.xpath("//button[normalize-space()='Add']");
    private final By cancelButton = By.xpath("//button[normalize-space()='Cancel']");
    // Row-level selection checkboxes only -- excludes the header's "select all" checkbox, which
    // renders with the same ag-selection-checkbox class but lives outside any @row-index row.
    private final By firstSelectableCheckbox = By.xpath(
            "(//div[@role='row' and @row-index]//div[contains(@class,'ag-selection-checkbox')])[1]");

    public AddTeamMembersModal(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(addButton)).isDisplayed();
    }

    public void search(String value) {
        type(searchBox, value);
    }

    public void selectFirstAvailableUser() {
        click(firstSelectableCheckbox);
    }

    public void confirmAdd() {
        click(addButton);
    }

    public void cancel() {
        click(cancelButton);
    }
}
