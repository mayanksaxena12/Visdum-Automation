package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.ConfigReader;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for Organization Management & 3-Step Creation Wizard.
 * Mirrors src/app/pages/organization/OrganizationActionModals/OrganizationForm.tsx
 */
public class OrganizationPage extends BasePage {

    // Toolbar & Navigation
    private final By addOrganizationBtn = By.xpath("//div[contains(@class,'btn-primary') and contains(.,'Add New Organization')] | //button[contains(.,'Add New Organization')]");
    private final By searchInput = By.xpath("//input[@data-dc-user-table-filter='search'] | //input[@placeholder='Search']");

    // Drawer Header & Containers
    private final By drawerContainer = By.xpath("//form[@id='add_organization_form'] | //input[@name='organization_name'] | //div[contains(@class,'drawer-on')]");
    private final By drawerTitle = By.xpath("//*[contains(@class,'text-primary') and (contains(text(),'Add New Organization') or contains(text(),'Organization'))]");

    // Step 1: Organization Details
    private final By orgNameInput = By.name("organization_name");
    private final By orgEmailInput = By.name("organization_email");
    private final By addressInput = By.name("address");
    private final By step1NextBtn = By.xpath("//button[@data-dc-stepper-action='next'] | //button[contains(.,'Next')]");

    // Step 2: Admin User Details
    private final By adminNameInput = By.name("name");
    private final By adminEmailInput = By.name("email");
    private final By adminAddressInput = By.name("address");
    private final By adminPhoneInput = By.name("phone_no");
    private final By step2NextBtn = By.xpath("//button[@data-dc-stepper-action='next'] | //button[contains(.,'Next')]");
    private final By step2BackBtn = By.xpath("//button[@data-dc-stepper-action='previous'] | //button[contains(.,'Previous')]");

    // Step 3: Admin Password
    private final By passwordInput = By.name("password");
    private final By confirmPasswordInput = By.name("confirm_password");
    private final By submitBtn = By.xpath("//button[@data-dc-stepper-action='submit'] | //button[normalize-space()='Submit']");

    // Validation & Alerts
    private final By validationError = By.xpath("//div[contains(@class,'fv-help-block')] | //*[@role='alert']");

    public OrganizationPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToOrganizations() {
        driver.get(ConfigReader.get("url") + "/organization");
    }

    public boolean isAddOrganizationButtonVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(addOrganizationBtn)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickAddOrganization() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(addOrganizationBtn));
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", btn);
                } catch (Exception ignored) {}

                if (attempt == 1) {
                    try {
                        btn.click();
                    } catch (Exception ex) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                    }
                } else if (attempt == 2) {
                    try {
                        new org.openqa.selenium.interactions.Actions(driver).moveToElement(btn).click().perform();
                    } catch (Exception ex) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                    }
                } else {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true, view: window}));", btn);
                }

                // Verify if drawer opened
                new WebDriverWait(driver, Duration.ofSeconds(4))
                        .until(ExpectedConditions.visibilityOfElementLocated(orgNameInput));
                return; // Drawer successfully opened!
            } catch (Exception e) {
                if (attempt == 3) {
                    System.out.println(">>> [WARN] clickAddOrganization: drawer did not open after 3 attempts.");
                } else {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    public boolean isDrawerOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(orgNameInput)).isDisplayed();
        } catch (Exception e) {
            return !driver.findElements(orgNameInput).isEmpty() && driver.findElement(orgNameInput).isDisplayed();
        }
    }

    public void enterStep1Details(String orgName, String orgEmail, String address) {
        enterStep1Details(orgName, orgEmail, address, null, null, null);
    }

    public void enterStep1Details(String orgName, String orgEmail, String address,
                                  String currency, String timezone, String financialYear) {
        if (orgName != null && !orgName.isBlank()) {
            type(orgNameInput, orgName);
        }
        if (orgEmail != null && !orgEmail.isBlank()) {
            type(orgEmailInput, orgEmail);
        }
        if (address != null && !address.isBlank()) {
            type(addressInput, address);
        }
        if (currency != null && !currency.isBlank()) {
            selectReactOption("Select Currency", currency);
        }
        if (timezone != null && !timezone.isBlank()) {
            selectReactOption("Select Timezone", timezone);
        }
        if (financialYear != null && !financialYear.isBlank()) {
            selectReactOption("Select Financial Year", financialYear);
        }
    }

    public void clickStep1Next() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(step1NextBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public boolean isStep2Visible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(adminNameInput)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void enterStep2Details(String adminName, String adminEmail, String phone) {
        enterStep2Details(adminName, adminEmail, null, phone);
    }

    public void enterStep2Details(String adminName, String adminEmail, String address, String phone) {
        if (adminName != null && !adminName.isBlank()) {
            type(adminNameInput, adminName);
        }
        if (adminEmail != null && !adminEmail.isBlank()) {
            type(adminEmailInput, adminEmail);
        }
        if (address != null && !address.isBlank()) {
            type(adminAddressInput, address);
        }
        if (phone != null && !phone.isBlank()) {
            type(adminPhoneInput, phone);
        }
    }

    public void clickStep2Next() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(step2NextBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public boolean isStep3Visible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void enterStep3Password(String password, String confirmPassword) {
        if (password != null && !password.isBlank()) {
            type(passwordInput, password);
        }
        if (confirmPassword != null && !confirmPassword.isBlank()) {
            type(confirmPasswordInput, confirmPassword);
        }
    }

    public void submitOrganizationCreation() {
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
        try {
            submit.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
        }
    }

    public boolean isValidationMessagePresent(String expectedMessage) {
        try {
            By locator = By.xpath("//div[contains(@class,'fv-help-block') and contains(.,'" + expectedMessage + "')] | //*[@role='alert' and contains(.,'" + expectedMessage + "')]");
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuccessToastVisible() {
        try {
            By toastLocator = By.xpath("//*[contains(@class,'Toastify') and (contains(.,'successfully') or contains(.,'Created') or contains(.,'created'))]");
            return wait.until(ExpectedConditions.visibilityOfElementLocated(toastLocator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void searchOrganization(String orgName) {
        WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        search.clear();
        search.sendKeys(orgName);
    }

    public boolean isOrganizationPresentInGrid(String orgName) {
        try {
            By orgCell = By.xpath("//div[@role='gridcell' and contains(.,'" + orgName + "')] | //td[contains(.,'" + orgName + "')]");
            return wait.until(ExpectedConditions.visibilityOfElementLocated(orgCell)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
