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


        // Sidebar Open
        wait.until(
                        ExpectedConditions.elementToBeClickable(sidebarToggle))
                .click();



        // Users Expand
        wait.until(
                        ExpectedConditions.elementToBeClickable(usersMenu))
                .click();

        // Employees Click
        wait.until(
                        ExpectedConditions.elementToBeClickable(employeesMenu))
                .click();




    }

    public void navigateToTeams() {

        // Sidebar Open
        wait.until(
                        ExpectedConditions.elementToBeClickable(sidebarToggle))
                .click();

        // Users Expand
        wait.until(
                        ExpectedConditions.elementToBeClickable(usersMenu))
                .click();

        // Teams Click
        wait.until(
                        ExpectedConditions.elementToBeClickable(teamsMenu))
                .click();
    }

    public void navigateToDepartments() {

        // Sidebar Open
        wait.until(
                        ExpectedConditions.elementToBeClickable(sidebarToggle))
                .click();

        // Users Expand
        wait.until(
                        ExpectedConditions.elementToBeClickable(usersMenu))
                .click();

        // Departments Click
        wait.until(
                        ExpectedConditions.elementToBeClickable(departmentsMenu))
                .click();
    }

      /** True once the authenticated dashboard is reachable (sidebar toggle present). */
    public boolean isLoaded() {
        return wait.until(ExpectedConditions.elementToBeClickable(sidebarToggle)).isDisplayed();
    }
}
