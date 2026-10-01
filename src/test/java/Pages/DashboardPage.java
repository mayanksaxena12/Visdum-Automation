package Pages;

//import java.io.File;
import java.time.Duration;
import org.openqa.selenium.By;
//import org.openqa.selenium.OutputType;
//import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
//import org.openqa.selenium.support.*;

public class DashboardPage {

    WebDriver driver;
    WebDriverWait wait;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // Sidebar
    By sidebarToggle =
            By.id("dc_app_sidebartoggle");

    // Users Menu
    By usersMenu =
            By.xpath("//span[normalize-space()='Users']");
    // Employees
    By employeesMenu =
            By.xpath("//a[@href='/users/employees']");

    // Teams (SidebarMenuMain.tsx -> SidebarMenuItem to='/users/teams')
    By teamsMenu =
            By.xpath("//a[@href='/users/teams']");

    // Departments (SidebarMenuMain.tsx -> SidebarMenuItem to='users/department')
    By departmentsMenu =
            By.xpath("//a[contains(@href,'users/department')]");

    public void navigateToEmployees() {
        driver.get(utilities.ConfigReader.get("url") + "/users/employees");
    }

    public void navigateToTeams() {
        driver.get(utilities.ConfigReader.get("url") + "/users/teams");
    }

    public void navigateToDepartments() {
        driver.get(utilities.ConfigReader.get("url") + "/users/department");
    }

    // Data Menu Locators
    By dataMenu = By.xpath("//span[normalize-space()='Data']");
    By rawDataMenu = By.xpath("//a[@href='/data/raw-data']");
    By dealCreditsMenu = By.xpath("//a[@href='/data/deal-credits']");
    By viewDataStreamsBtn = By.xpath("//button[normalize-space()='View']");

    public void navigateToDataStreams() {
        driver.get(utilities.ConfigReader.get("url") + "/data/data-streams");
    }

    public void navigateToRawData() {
        driver.get(utilities.ConfigReader.get("url") + "/data/raw-data");
    }

    public void navigateToDealCredits() {
        driver.get(utilities.ConfigReader.get("url") + "/data/deal-credits");
    }

    public void navigateToPlans() {
        driver.get(utilities.ConfigReader.get("url") + "/plans/create-plan");
    }

    public void navigateToRateTables() {
        driver.get(utilities.ConfigReader.get("url") + "/plans/rate-table");
    }

    public void navigateToAssignPlans() {
        driver.get(utilities.ConfigReader.get("url") + "/plans/assign-plan");
    }

    public void navigateToEsign() {
        driver.get(utilities.ConfigReader.get("url") + "/plans/e-sign");
    }

    // Settings / Resource Locators
    private final By settingsLink = By.xpath("//a[@href='/settings']");
    private final By resourceTab = By.xpath("//a[normalize-space()='Resource' or contains(@href,'Resource')]");

    public void navigateToResources() {
        isLoaded();
        try {
            driver.get(utilities.ConfigReader.get("url") + "/settings");
            wait.until(ExpectedConditions.elementToBeClickable(resourceTab)).click();
        } catch (Exception e) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(settingsLink)).click();
                wait.until(ExpectedConditions.elementToBeClickable(resourceTab)).click();
            } catch (Exception ex) {
                driver.get(utilities.ConfigReader.get("url") + "/settings");
            }
        }
    }


      /** True once the authenticated dashboard is reachable (sidebar toggle present). */
    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(sidebarToggle)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
