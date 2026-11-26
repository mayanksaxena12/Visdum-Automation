package Pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * User Data Stream: the "View User Stream" drawer and the 4-step wizard
 * (pages/users/user-stream, pages/users/view-user-stream).
 *
 * <ul>
 *   <li>Steps: Stream Details (Choose App) → User Mapping &amp; Ordering → Define Criteria → Review &amp; Submit.
 *       HubSpot has no Define Criteria step.</li>
 *   <li>Footer: Cancel (step 1) / Previous, and a primary Next / Submit button.</li>
 *   <li>Dropdowns are react-select with classNamePrefix "select", except the criteria
 *       "Condition" (AND/OR) select which uses react-select's default css-* classes.</li>
 * </ul>
 * Nothing here clicks Submit: that would overwrite the tenant's user stream.
 */
public class UserStreamWizardPage extends BasePage {

    private final By nextBtn = By.xpath("//button[contains(@class,'btn-primary')][.//span[normalize-space()='Next']]");
    private final By previousBtn = By.xpath("//button[normalize-space()='Previous']");
    private final By stepTitle = By.cssSelector("h3.text-gray-700.fw-bold");
    private final By addCriteriaBtn = By.xpath("//button[contains(normalize-space(),'Add New Criteria')]");
    private final By criteriaTitles = By.xpath("//span[contains(@class,'fs-3') and starts-with(normalize-space(),'Criteria ')]");
    private final By targetFields = By.cssSelector("span.form-control.input-read-only");

    public UserStreamWizardPage(WebDriver driver) {
        super(driver);
    }

    // ---------------- View User Stream drawer ----------------

    /** True when the "View User Stream" drawer is open (a stream exists). */
    public boolean isViewDrawerOpen() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(8)).until(d -> d.findElements(
                    By.xpath("//*[normalize-space()='View User Stream']")).stream().anyMatch(WebElement::isDisplayed));
        } catch (Exception e) {
            return false;
        }
    }

    /** Platform / base view name shown next to "User Source". */
    public String userSourceName() {
        return text(By.xpath("//span[normalize-space()='User Source']/following::span[contains(@class,'fw-bold')][1]")).trim();
    }

    /** Mapping rows under "User Column & Orders": {sourceColumn, targetField(with * if required)}. */
    public List<String[]> mappingRows() {
        firstVisible(By.xpath("//*[normalize-space()='User Column & Orders']"));
        List<String[]> rows = new ArrayList<>();
        for (WebElement src : driver.findElements(By.cssSelector("span.text-gray-600.fs-4.w-200px"))) {
            if (!src.isDisplayed()) {
                continue;
            }
            WebElement target = src.findElement(By.xpath("following-sibling::span[1]"));
            rows.add(new String[] {src.getText().trim(), target.getText().trim()});
        }
        recordStep("User Column & Orders has " + rows.size() + " mappings");
        return rows;
    }

    /** The drawer's "Edit" footer button (a div styled as a button). */
    public void clickViewEdit() {
        click(By.xpath("//div[contains(@class,'btn-primary') and normalize-space()='Edit']"));
    }

    /** The drawer's "Back" footer button. */
    public void clickViewBack() {
        click(By.xpath("//*[(self::button or contains(@class,'btn')) and normalize-space()='Back']"));
    }

    /** True once the View User Stream drawer has closed (up to 5s for its slide-out). */
    public boolean isViewDrawerClosed() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> d.findElements(
                    By.xpath("//*[normalize-space()='View User Stream']")).stream().noneMatch(WebElement::isDisplayed));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSectionVisible(String header) {
        return anyVisible(By.xpath("//*[normalize-space()=" + xpathLiteral(header) + "]"));
    }

    // ---------------- Wizard ----------------

    public void openWizard(String baseUrl, boolean editExisting) {
        driver.get(baseUrl + (editExisting ? "/users/user-streams/edit" : "/users/user-streams/add"));
        firstVisible(stepTitle);
    }

    public String currentStepTitle() {
        return text(stepTitle).trim();
    }

    /** Waits until the step title becomes {@code title}; false if it does not within ~15s. */
    public boolean waitForStep(String title) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> d.findElements(stepTitle).stream()
                    .anyMatch(e -> e.isDisplayed() && e.getText().trim().equals(title)));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isStepperCurrent(String stepName) {
        return anyVisible(By.xpath("//div[contains(@class,'stepper-item') and contains(@class,'current')]"
                + "//h3[contains(@class,'stepper-title') and normalize-space()=" + xpathLiteral(stepName) + "]"));
    }

    public boolean isStepperCompleted(String stepName) {
        return anyVisible(By.xpath("//div[contains(@class,'stepper-item') and contains(@class,'completed')]"
                + "//h3[contains(@class,'stepper-title') and normalize-space()=" + xpathLiteral(stepName) + "]"));
    }

    public void next() {
        click(nextBtn);
    }

    public void previous() {
        click(previousBtn);
    }

    public boolean isToastVisible(String textStart) {
        return anyVisible(By.xpath("//*[contains(normalize-space()," + xpathLiteral(textStart) + ")]"));
    }

    // ---- Step 1 "Source type" cards (1_UserStreamDetails.tsx): "Connected app" or "Base view" ----

    private By sourceCard(String title) {
        return By.xpath("//div[@role='button'][.//div[contains(@class,'fw-bold') and normalize-space()=" + xpathLiteral(title) + "]]");
    }

    /** Picks the source type card ("Connected app" or "Base view"); a no-op if it is already selected. */
    public void chooseSourceType(String title) {
        if (!isSourceTypeSelected(title)) {
            click(sourceCard(title));
            recordStep("Source type: " + title);
        }
    }

    /** The selected card has a primary border (and a check icon). */
    public boolean isSourceTypeSelected(String title) {
        return driver.findElements(sourceCard(title)).stream()
                .anyMatch(c -> c.isDisplayed() && c.getAttribute("class").contains("border-primary"));
    }

    /**
     * True when the browser itself blocks Next because a required react-select (by its {@code name},
     * e.g. "platform_id" for Choose App) is empty: react-select renders a hidden {@code required}
     * input, so the browser shows "Please fill out this field." and the app's own toast never fires.
     */
    public boolean isRequiredSelectEmpty(String name) {
        Object missing = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "var i = document.querySelector(\"input[name='\" + arguments[0] + \"'][required]\");"
                        + " return i ? i.validity.valueMissing : null;", name);
        return Boolean.TRUE.equals(missing);
    }

    /** Option labels of the "Choose App" dropdown (connected CRMs). */
    public List<String> appOptions() {
        openReactSelect(selectedAppOr("Choose App"));
        List<String> opts = optionTexts(".select__menu .select__option");
        driver.switchTo().activeElement().sendKeys(org.openqa.selenium.Keys.ESCAPE);
        return opts;
    }

    /** Selects an app by its label in "Choose App"; returns the label now shown. */
    public String chooseApp(String label) {
        openReactSelect(selectedAppOr("Choose App"));
        firstClickable(By.xpath("//div[contains(@class,'select__option') and normalize-space()=" + xpathLiteral(label) + "]")).click();
        recordStep("Chose app '" + label + "'");
        return selectedApp();
    }

    /** Selects the first available app. */
    public String chooseFirstApp() {
        openReactSelect(selectedAppOr("Choose App"));
        WebElement first = firstClickable(By.cssSelector(".select__menu .select__option"));
        String label = first.getText().trim();
        first.click();
        recordStep("Chose app '" + label + "'");
        return label;
    }

    public String selectedApp() {
        List<WebElement> v = driver.findElements(By.xpath("//label[normalize-space()='Choose App']/following::div[contains(@class,'select__single-value')][1]"));
        return v.isEmpty() ? "" : v.get(0).getText().trim();
    }

    /**
     * The chosen app once Step 1 has re-rendered it (up to 10s): coming back with Previous, Step 1
     * reloads the connected-app list before it can show the selection again. "" if none appears.
     */
    public String waitForSelectedApp() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(10))
                    .ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                    .until(d -> {
                        String s = selectedApp();
                        return s.isEmpty() ? null : s;
                    });
        } catch (org.openqa.selenium.TimeoutException e) {
            return "";
        }
    }

    private String selectedAppOr(String placeholder) {
        String s = selectedApp();
        return s.isEmpty() ? placeholder : s;
    }

    /** Visdum target field names on the mapping step (with '*' when required). */
    public List<String> targetFieldNames() {
        firstVisible(targetFields);
        List<String> names = new ArrayList<>();
        for (WebElement f : driver.findElements(targetFields)) {
            if (f.isDisplayed()) {
                // 2_UserDataMapping.tsx: <span class='form-control input-read-only'>first_name<span class='text-danger'>*</span></span>
                String name = f.getText().replace("*", "").trim();
                boolean required = f.findElements(By.xpath("./span[contains(@class,'text-danger') and normalize-space()='*']")).size() > 0;
                names.add(name + (required ? "*" : ""));
            }
        }
        return names;
    }

    /** The mapping row (div.d-flex.py-4) whose Visdum field is {@code target} (its '*' ignored). */
    private By mappingRow(String target) {
        return By.xpath("//div[contains(@class,'d-flex') and contains(@class,'py-4')]"
                + "[.//span[contains(@class,'input-read-only') and normalize-space(translate(normalize-space(.),'*',''))="
                + xpathLiteral(target) + "]]");
    }

    // In a mapping row the source-column select comes first; the data type select is disabled and
    // carries the extra class 'input-read-only'. An unmapped row shows only the data type's value,
    // so each value must be read from its own select, not by position.
    private static final By SOURCE_SELECT = By.xpath(".//div[contains(@class,'basic-multi-select') and not(contains(@class,'input-read-only'))]");

    /** Value shown in a mapping row's source-column select, or its data type select. */
    private static String valueIn(WebElement row, boolean dataType) {
        String which = dataType ? "contains(@class,'input-read-only')" : "not(contains(@class,'input-read-only'))";
        List<WebElement> v = row.findElements(By.xpath(".//div[contains(@class,'basic-multi-select') and " + which + "]"
                + "//div[contains(@class,'select__single-value')]"));
        return v.isEmpty() ? "" : v.get(0).getText().trim();
    }

    /** Opens the source-column select of a mapping row and returns its option labels. */
    public List<String> sourceOptions(String target) {
        WebElement row = firstVisible(mappingRow(target));
        row.findElement(SOURCE_SELECT).findElement(By.cssSelector(".select__control")).click();
        List<String> opts = optionTexts(".select__menu .select__option");
        driver.switchTo().activeElement().sendKeys(org.openqa.selenium.Keys.ESCAPE);
        return opts;
    }

    /** Maps {@code target} to the source column with the same name if offered, else the first option. */
    public String mapField(String target) {
        WebElement row = firstVisible(mappingRow(target));
        row.findElement(SOURCE_SELECT).findElement(By.cssSelector(".select__control")).click();
        List<WebElement> opts = new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            List<WebElement> o = d.findElements(By.cssSelector(".select__menu .select__option"));
            return o.isEmpty() ? null : o;
        });
        WebElement pick = opts.stream().filter(o -> o.getText().trim().equalsIgnoreCase(target)).findFirst().orElse(opts.get(0));
        String label = pick.getText().trim();
        pick.click();
        recordStep("Mapped " + target + " <- " + label);
        return mappedSource(target);
    }

    public String mappedSource(String target) {
        return valueIn(firstVisible(mappingRow(target)), false);
    }

    /** Text shown in the (read-only) data type select of a mapping row. */
    public String dataTypeOf(String target) {
        return valueIn(firstVisible(mappingRow(target)), true);
    }

    /** Clears a mapping row's source column (react-select clear "x"). */
    public void clearMapping(String target) {
        WebElement row = firstVisible(mappingRow(target));
        List<WebElement> clear = row.findElement(SOURCE_SELECT).findElements(By.cssSelector(".select__clear-indicator"));
        if (!clear.isEmpty()) {
            clear.get(0).click();
            recordStep("Cleared mapping for " + target);
        }
    }

    // ---------------- Define Criteria ----------------

    public boolean isAddCriteriaVisible() {
        return anyVisible(addCriteriaBtn);
    }

    public void addCriteria() {
        click(addCriteriaBtn);
    }

    public int criteriaCount() {
        return (int) driver.findElements(criteriaTitles).stream().filter(WebElement::isDisplayed).count();
    }

    public void deleteFirstCriteria() {
        firstVisible(By.cssSelector("span.bg-white.rounded.p-3.ms-2 i.fa-trash-can")).click();
        recordStep("Deleted first criteria");
    }

    /** The react-select under a criteria rule label ("Filter", "Operator") of the first criteria block. */
    private By ruleSelect(String label) {
        return By.xpath("(//label[normalize-space()=" + xpathLiteral(label) + "]/following::div[contains(@class,'select__control')][1])[1]");
    }

    /** Opens the first rule's "Filter" dropdown and returns its options (the source columns), then closes it. */
    public List<String> criteriaFilterOptions() {
        click(ruleSelect("Filter"));
        List<String> opts = optionTexts(".select__menu .select__option, .select__menu [id*='-option-']");
        driver.switchTo().activeElement().sendKeys(org.openqa.selenium.Keys.ESCAPE);
        return opts;
    }

    /** Completes the first criteria rule (first Filter, first Operator, value "1") so Next can proceed. */
    public void fillFirstCriteriaRule() {
        for (String label : new String[] {"Filter", "Operator"}) {
            click(ruleSelect(label));
            firstClickable(REACT_OPTION).click();
        }
        List<WebElement> value = driver.findElements(By.cssSelector("input[placeholder='Value']"));
        if (!value.isEmpty() && value.get(0).isDisplayed()) {
            type(By.cssSelector("input[placeholder='Value']"), "1");
        }
        recordStep("Filled the first criteria rule");
    }

    public boolean isOperatorDropdownVisible() {
        return anyVisible(By.xpath("//*[contains(@class,'select__placeholder') and normalize-space()='Select Data Type']"));
    }

    public void typeComparisonValue(String value) {
        type(By.cssSelector("input[placeholder='Value']"), value);
    }

    public String comparisonValue() {
        return firstVisible(By.cssSelector("input[placeholder='Value']")).getAttribute("value");
    }

    /** Opens the AND/OR "Condition" select between criteria and returns its options. */
    public List<String> conditionOptions() {
        openCondition();
        List<String> opts = optionTexts("[id*='-option-']");
        driver.switchTo().activeElement().sendKeys(org.openqa.selenium.Keys.ESCAPE);
        return opts;
    }

    public String chooseCondition(String andOr) {
        openCondition();
        firstClickable(By.xpath("//*[contains(@id,'-option-') and normalize-space()=" + xpathLiteral(andOr) + "]")).click();
        recordStep("Chose condition " + andOr);
        WebElement control = firstVisible(By.xpath("//label[normalize-space()='Condition']/following::div[contains(@class,'-control')][1]"));
        return control.getText().trim();
    }

    private void openCondition() {
        firstClickable(By.xpath("//label[normalize-space()='Condition']/following::div[contains(@class,'-control')][1]")).click();
    }

    private List<String> optionTexts(String css) {
        List<WebElement> els = new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            List<WebElement> o = d.findElements(By.cssSelector(css));
            return o.isEmpty() ? null : o;
        });
        List<String> out = new ArrayList<>();
        for (WebElement e : els) {
            out.add(e.getText().trim());
        }
        recordStep("Dropdown options: " + out);
        return out;
    }

    /** Waits until a step's own content is ready (no exception if it never appears). */
    public void waitVisible(By locator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception ignored) {
        }
    }
}
