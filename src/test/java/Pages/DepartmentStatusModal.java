package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Deactivate/Activate confirmation modal from DepartmentsList.tsx (mirrors TeamStatusModal). */
public class DepartmentStatusModal extends BasePage {

    private final By proceed = By.xpath("//button[normalize-space()='Proceed']");
    private final By cancel = By.xpath("//button[normalize-space()='Cancel']");

    public DepartmentStatusModal(WebDriver driver) {
        super(driver);
    }

    public boolean isConfirmationVisible(String expectedPrompt) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[normalize-space()=" + xpathLiteral(expectedPrompt) + "]"))).isDisplayed();
    }

    public void confirm() {
        click(proceed);
    }

    public void dismiss() {
        click(cancel);
    }
}
