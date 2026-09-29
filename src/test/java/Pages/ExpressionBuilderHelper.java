package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Reusable helper for interacting with Visdum's custom Expression & Formula Rule Builder.
 * Supports tokenized formula building, operator insertions, bracket balancing,
 * and formula validation assertions.
 */
public class ExpressionBuilderHelper extends BasePage {

    private final By expressionContainer = By.xpath("//div[contains(@class,'vis-form-controls') or contains(@class,'expression-builder')]");
    private final By activeInput = By.xpath("//input[contains(@class,'form-control') or @placeholder='Enter Expression' or contains(@class,'variable-input') or contains(@class,'number-input')]");
    private final By contentEditableBox = By.xpath("//div[@contenteditable='true']");
    private final By errorMessageContainer = By.xpath("//div[@data-error-messages or contains(@class,'text-danger') or contains(@style,'#F1416C')]");

    public ExpressionBuilderHelper(WebDriver driver) {
        super(driver);
    }

    /** Focuses the primary formula input area */
    public void focusBuilder() {
        if (!driver.findElements(contentEditableBox).isEmpty()) {
            click(contentEditableBox);
        } else {
            click(activeInput);
        }
    }

    /** Types a token or variable name into the formula builder */
    public void typeToken(String token) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(
                !driver.findElements(contentEditableBox).isEmpty() ? contentEditableBox : activeInput));
        element.sendKeys(token);
    }

    /** Types an arithmetic operator (+, -, *, /) */
    public void insertOperator(String operator) {
        WebElement element = driver.switchTo().activeElement();
        element.sendKeys(operator);
    }

    /** Enters opening bracket '(' */
    public void insertOpeningBracket() {
        WebElement element = driver.switchTo().activeElement();
        element.sendKeys("(");
    }

    /** Enters closing bracket ')' */
    public void insertClosingBracket() {
        WebElement element = driver.switchTo().activeElement();
        element.sendKeys(")");
    }

    /** Selects a variable or metric from the open autocomplete palette */
    public void selectPaletteVariable(String variableName) {
        By paletteItem = By.xpath("//div[contains(@class,'palette') or contains(@class,'dropdown')]//*[normalize-space()="
                + xpathLiteral(variableName) + "]");
        wait.until(ExpectedConditions.elementToBeClickable(paletteItem)).click();
    }

    /** Clears the current expression builder */
    public void clearBuilder() {
        WebElement element = !driver.findElements(contentEditableBox).isEmpty()
                ? driver.findElement(contentEditableBox)
                : driver.findElement(activeInput);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
    }

    /** True if formula syntax errors are currently rendered on screen */
    public boolean hasSyntaxError() {
        List<WebElement> errors = driver.findElements(errorMessageContainer);
        return errors.stream().anyMatch(e -> e.isDisplayed() && !e.getText().trim().isEmpty());
    }

    /** Returns the formula error text if present */
    public String getSyntaxErrorMessage() {
        if (hasSyntaxError()) {
            return text(errorMessageContainer).trim();
        }
        return "";
    }
}
