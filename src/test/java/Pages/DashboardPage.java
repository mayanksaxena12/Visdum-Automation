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
        isLoaded();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(sidebarToggle)).click();
            wait.until(ExpectedConditions.elementToBeClickable(usersMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(employeesMenu)).click();
        } catch (Exception e) {
            driver.get(utilities.ConfigReader.get("url") + "/users/employees");
        }
    }

    public void navigateToTeams() {
        isLoaded();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(sidebarToggle)).click();
            wait.until(ExpectedConditions.elementToBeClickable(usersMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(teamsMenu)).click();
        } catch (Exception e) {
            driver.get(utilities.ConfigReader.get("url") + "/users/teams");
        }
    }

    public void navigateToDepartments() {
        isLoaded();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(sidebarToggle)).click();
            wait.until(ExpectedConditions.elementToBeClickable(usersMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(departmentsMenu)).click();
        } catch (Exception e) {
            driver.get(utilities.ConfigReader.get("url") + "/users/department");
        }
    }

    // Data Menu Locators
    By dataMenu = By.xpath("//span[normalize-space()='Data']");
    By rawDataMenu = By.xpath("//a[@href='/data/raw-data']");
    By viewDataStreamsBtn = By.xpath("//button[normalize-space()='View']");

    public void navigateToDataStreams() {
        isLoaded();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(sidebarToggle)).click();
            wait.until(ExpectedConditions.elementToBeClickable(dataMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(rawDataMenu)).click();
            wait.until(ExpectedConditions.elementToBeClickable(viewDataStreamsBtn)).click();
        } catch (Exception e) {
            // Direct route navigation fallback
            driver.get(utilities.ConfigReader.get("url") + "/data/data-streams");
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
