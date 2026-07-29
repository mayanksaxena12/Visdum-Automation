package Pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EmployeesPage {

    WebDriver driver;
    WebDriverWait wait;

    public EmployeesPage(WebDriver driver) {

        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    By searchBox =
            By.xpath("//input[@placeholder='Search']");

    By addUser =
            By.xpath("//*[contains(text(),'Add New User')]");

    public void search(String user) {
        WebElement search =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                searchBox));


        search.clear();
        search.sendKeys(user);

        System.out.println("User searched: " + user);
    }

    public void clickAddUser() {

        wait.until(
                ExpectedConditions.elementToBeClickable(addUser)).click();

        System.out.println("Add User button clicked");


    }
}
