package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Read-only user drawer from UserActionModals/UserView.tsx.
 *
 * <p>Each field is an {@code <li>} holding two spans: a label span ({@code fs-6 text-gray-500}) and
 * a value span ({@code fs-6} without the muted class). We locate the value by finding the list item
 * whose label matches, then selecting the sibling {@code fs-6} span that is not the muted label.
 */
public class UserViewPage extends BasePage {

    private final By backButton = By.xpath("//button[normalize-space()='Back']");
    private final By viewManagerHistoryLink = By.xpath("//a[normalize-space()='View Manager History']");
    private final By managerHistoryHeader = By.xpath("//h4[normalize-space()='Manager History']");
    private final By closeManagerHistoryBtn = By.xpath("//div[./h4[normalize-space()='Manager History']]//button");

    public UserViewPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(backButton)).isDisplayed();
    }

    private String value(String label) {
        // The value is the element right after its label ("Name" -> "QA alertoff ..."); the old
        // class-based match did not fit the Name row on the live page.
        String l = xpathLiteral(label);
        By candidates = By.xpath("//*[normalize-space()=" + l + " and not(*)]/following-sibling::*[not(self::br)][1]"
                + " | //li[.//span[normalize-space()=" + l + "]]//span[contains(@class,'fs-6') and not(contains(@class,'text-gray-500'))]");
        // Other (hidden) lists on the page can have the same label, so take the first VISIBLE one.
        return wait.ignoring(org.openqa.selenium.StaleElementReferenceException.class).until(d -> d.findElements(candidates).stream()
                .filter(WebElement::isDisplayed).map(WebElement::getText).findFirst().orElse(null));
    }

    /** Value shown next to a label in the view drawer (e.g. "Last Updated At"). */
    public String getField(String label) {
        return value(label).trim();
    }

    public String getName() {
        return value("Name");
    }

    public String getEmail() {
        return value("E-mail");
    }

    public String getReferenceId() {
        return value("User Reference ID");
    }

    public String getRole() {
        return value("Role");
    }

    public String getCurrency() {
        return value("Currency");
    }

    public String getEmployeeId() {
        return value("Employee Id");
    }

    public String getJoiningDate() {
        return value("Joining Date");
    }

    public String getDesignation() {
        return value("Designation");
    }

    public String getBankDetails() {
        return value("Bank Details");
    }

    public String getTaxDetails() {
        return value("Tax Details");
    }

    public String getManager() {
        return value("Manager");
    }

    public String getManagerEffectiveDate() {
        return value("Manager Effective Date");
    }

    public String getTeam() {
        return value("Team");
    }

    public String getDepartment() {
        return value("Department");
    }

    public void openManagerHistory() {
        click(viewManagerHistoryLink);
    }

    public boolean isManagerHistoryOpen() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(managerHistoryHeader)).isDisplayed();
    }

    public void closeManagerHistory() {
        click(closeManagerHistoryBtn);
    }

    public void close() {
        click(backButton);
    }

    // ---- helpers for VU-007/VU-010 and MH-002..MH-005 ----
    public boolean hasManagerHistoryLink() {
        return anyVisible(By.xpath("//a[normalize-space()='View Manager History']"));
    }

    /** Manager History entries in display order: {timestamp, managerName}. */
    public java.util.List<String[]> managerHistoryEntries() {
        firstVisible(By.xpath("//h4[normalize-space()='Manager History']"));
        java.util.List<String[]> out = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement card : driver.findElements(By.cssSelector("div[id^='version_']"))) {
            if (!card.isDisplayed()) {
                continue;
            }
            String ts = card.findElement(By.cssSelector("span.text-gray-400")).getText().trim();
            String name = card.findElement(By.cssSelector("button.accordion-button")).getText().trim();
            out.add(new String[] {ts, name});
        }
        recordStep("Manager History has " + out.size() + " entries");
        return out;
    }

    public boolean isClosed() {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//button[normalize-space()='Back']")));
        } catch (Exception e) {
            return false;
        }
    }

}

