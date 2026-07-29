package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Read-only department drawer from DepartmentActionModals/DepartmentView.tsx (mirrors TeamViewPage).
 * The name value is an {@code h3} and the description value a {@code p}, each following its muted
 * label span; the "Back" action is a {@code div}, not a {@code button}.
 */
public class DepartmentViewPage extends BasePage {

    private final By backButton = By.xpath("//*[normalize-space()='Back']");
    private final By name =
            By.xpath("//span[normalize-space()='Department Name']/following-sibling::h3[1]");
    private final By description =
            By.xpath("//span[normalize-space()='Department Description']/following-sibling::p[1]");

    public DepartmentViewPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(backButton)).isDisplayed();
    }

    public String getName() {
        return text(name);
    }

    public String getDescription() {
        return text(description);
    }

    public void close() {
        click(backButton);
    }
}
