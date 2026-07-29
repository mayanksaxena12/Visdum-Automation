package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Read-only team drawer from TeamActionModals/TeamView.tsx.
 *
 * <p>Unlike {@link UserViewPage}, each field's label span ({@code fs-6 text-gray-500}) is followed
 * by a differently-tagged value node ({@code h3} for the name, {@code p.fw-bold} for the
 * description) rather than a matching sibling span, and the "Back" action is a {@code div}, not a
 * {@code button}.
 */
public class TeamViewPage extends BasePage {

    private final By backButton = By.xpath("//*[normalize-space()='Back']");
    private final By name = By.xpath("//span[normalize-space()='Team Name']/following-sibling::h3[1]");
    private final By description =
            By.xpath("//span[normalize-space()='Team Description']/following-sibling::p[1]");

    public TeamViewPage(WebDriver driver) {
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
