package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Fetch Users page object (from fetch-users/FetchUsersList.tsx).
 *
 * <p>Lists users fetched from external streams/integrations. Allows selecting rows,
 * assigning roles via cell editors, and saving/storing fetched users.
 */
public class FetchUsersPage extends BasePage {

    private final By fetchUsersHeader = By.xpath("//*[contains(text(),'Fetch Users') or contains(text(),'User Stream')]");
    private final By selectAllCheckbox = By.xpath("(//div[contains(@class,'ag-header-select-all') or contains(@class,'ag-checkbox')])[1]");
    private final By addSelectedUsersBtn = By.xpath("//button[contains(normalize-space(),'Add User') or contains(normalize-space(),'Store Users')]");
    private final By searchInput = By.cssSelector("input[placeholder='Search'], input[placeholder='Search User']");

    public FetchUsersPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(fetchUsersHeader)).isDisplayed();
    }

    public void search(String keyword) {
        type(searchInput, keyword);
    }

    public void selectAllUsers() {
        click(selectAllCheckbox);
    }

    public void submitSelectedUsers() {
        click(addSelectedUsersBtn);
    }

    // ---- helpers for FU-001..FU-010 (read-only: Add Users is never clicked) ----

    /** True once the Fetch Users page is shown (URL + grid). */
    public boolean isFetchUsersPage() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(d -> d.getCurrentUrl().contains("fetch-users"));
            return anyVisible(By.cssSelector(".fetch-users-grid, .ag-root-wrapper"));
        } catch (Exception e) {
            return false;
        }
    }

    public java.util.List<String> columnHeaders() {
        java.util.List<String> out = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement h : driver.findElements(By.cssSelector(".ag-header-cell-text"))) {
            if (h.isDisplayed() && !h.getText().isBlank()) {
                out.add(h.getText().trim());
            }
        }
        return out;
    }

    /**
     * Legend in FetchUsersList.tsx: {@code <span>[<span class='bullet bullet-dot bg-success'/>] New data</span>},
     * i.e. the label is a text node of the span that also holds the coloured dot.
     */
    public boolean isLegendVisible(String label, String bulletClass) {
        String dot = "span[contains(@class,'bullet-dot') and contains(@class," + xpathLiteral(bulletClass) + ")]";
        return anyVisible(By.xpath("//span[normalize-space()=" + xpathLiteral(label) + "][" + dot + "]"
                + " | //" + dot + "/following-sibling::*[1][normalize-space()=" + xpathLiteral(label) + "]"));
    }

    public int rowsWithIndicator(String bulletClass) {
        waitForRows();
        return driver.findElements(By.cssSelector(".ag-row span.bullet-dot." + bulletClass)).size();
    }

    /** Waits (up to 20s) for the fetched rows or the "no rows" overlay: users are loaded from the source after the page opens. */
    private void waitForRows() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20)).until(d ->
                    !d.findElements(By.cssSelector(".ag-center-cols-container .ag-row, .ag-overlay-no-rows-wrapper, .ag-overlay-no-rows-center")).isEmpty());
        } catch (org.openqa.selenium.TimeoutException ignored) {
        }
    }

    public int rowCount() {
        waitForRows();
        return (int) driver.findElements(By.cssSelector(".ag-center-cols-container .ag-row")).stream().filter(e -> e.isDisplayed()).count();
    }

    /** Text of each displayed row, joined across the pinned/center containers, keyed by row-index. */
    private java.util.Map<String, String> rowTexts() {
        java.util.Map<String, StringBuilder> byRow = new java.util.LinkedHashMap<>();
        for (org.openqa.selenium.WebElement r : driver.findElements(By.cssSelector(".ag-row[row-index]"))) {
            byRow.computeIfAbsent(r.getAttribute("row-index"), k -> new StringBuilder()).append(' ').append(r.getText());
        }
        java.util.Map<String, String> out = new java.util.LinkedHashMap<>();
        byRow.forEach((k, v) -> out.put(k, v.toString().replaceAll("\\s+", " ").trim()));
        return out;
    }

    /** First fetched user's e-mail (a cell containing '@'), or "" when there are no rows. */
    public String firstRowEmail() {
        waitForRows();
        for (String text : rowTexts().values()) {
            for (String word : text.split(" ")) {
                if (word.contains("@")) {
                    return word;
                }
            }
        }
        return "";
    }

    /** Types into the search box and returns true once every row shown contains {@code term} (up to 15s). */
    public boolean searchAndCheckRows(String term) {
        search(term);
        String t = term.toLowerCase();
        try {
            return new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                    .until(d -> {
                        java.util.Map<String, String> rows = rowTexts();
                        return !rows.isEmpty() && rows.values().stream().allMatch(r -> r.toLowerCase().contains(t));
                    });
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public int selectedRowCount() {
        return (int) driver.findElements(By.cssSelector(".ag-center-cols-container .ag-row.ag-row-selected")).stream().filter(e -> e.isDisplayed()).count();
    }

    // AG Grid's <input class='ag-checkbox-input'> is transparent (opacity 0), so Selenium treats it as
    // hidden; the visible, clickable part is its wrapper.
    public void tickFirstRow() {
        waitForRows();
        clickCheckbox(By.cssSelector(".ag-row[row-index='0'] .ag-selection-checkbox .ag-checkbox-input-wrapper,"
                + " .ag-row[row-index='0'] .ag-checkbox-input-wrapper"));
        recordStep("Ticked first row");
    }

    public void tickHeaderCheckbox() {
        waitForRows();
        clickCheckbox(By.cssSelector(".ag-header .ag-header-select-all .ag-checkbox-input-wrapper, .ag-header-select-all"));
        recordStep("Ticked header checkbox");
    }

    private void clickCheckbox(By wrapper) {
        org.openqa.selenium.WebElement box = firstVisible(wrapper);
        try {
            box.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", box);
        }
    }

    public boolean isAddUsersEnabled() {
        return firstVisible(By.xpath("//button[contains(@class,'btn-primary')][.//span[normalize-space()='Add Users'] or normalize-space()='Add Users']")).isEnabled();
    }

    public String totalRecordsText() {
        return firstVisible(By.cssSelector(".vis-total-records-panel")).getText().replaceAll("\s+", " ").trim();
    }

    public void clickBack() {
        firstVisible(By.cssSelector("img[alt='backMe']")).click();
        recordStep("Clicked Back");
    }

}
