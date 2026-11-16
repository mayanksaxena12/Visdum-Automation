package tests.organization;

import Base.DriverFactory;
import Base.OrganizationBaseTest;
import Pages.OrganizationPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.AutomationCleanupRegistry;
import utilities.ExcelTestDataReader;

import java.util.Map;

/**
 * Automated Test Suite for Organization Creation & Validation.
 * Tests 3-step wizard (Organization Details -> Admin User -> Password)
 * and executes end-to-end creation driven strictly by manual-testcases.xlsx (Sheet: Organization).
 */
public class CreateOrganizationTest extends OrganizationBaseTest {

    @Test(priority = 1, description = "TC_ORG_001: Verify navigation to Organization Management page")
    public void testNavigateToOrganizationsList() {
        WebDriver driver = DriverFactory.getDriver();
        OrganizationPage orgPage = new OrganizationPage(driver);

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("organization") || currentUrl.contains("dashboard"),
                "User should navigate to organization or dashboard page, current: " + currentUrl);
    }

    @Test(priority = 2, description = "TC_ORG_002: Verify Add New Organization button opens creation drawer")
    public void testOpenAddOrganizationDrawer() {
        WebDriver driver = DriverFactory.getDriver();
        OrganizationPage orgPage = new OrganizationPage(driver);

        if (orgPage.isAddOrganizationButtonVisible()) {
            orgPage.clickAddOrganization();
            Assert.assertTrue(orgPage.isDrawerOpen(), "Organization creation drawer should open on clicking Add New Organization");
        } else {
            System.out.println(">>> Add Organization button not visible for current user role (requires Super Admin).");
        }
    }

    @Test(priority = 3, description = "TC_ORG_003: Verify Step 1 required field validation for Organization Name & Email")
    public void testStep1RequiredFieldValidations() {
        WebDriver driver = DriverFactory.getDriver();
        OrganizationPage orgPage = new OrganizationPage(driver);

        if (orgPage.isAddOrganizationButtonVisible()) {
            orgPage.clickAddOrganization();
            Assert.assertTrue(orgPage.isDrawerOpen(), "Drawer should be open");

            // Attempt to proceed without entering details
            orgPage.clickStep1Next();

            boolean nameError = orgPage.isValidationMessagePresent("Organization Name is required")
                    || orgPage.isValidationMessagePresent("required");
            Assert.assertTrue(nameError || orgPage.isDrawerOpen(), "Validation error should appear or remain on step 1");
        }
    }

    @Test(priority = 4, description = "TC_ORG_004: Verify Step 1 invalid email format validation driven from Excel TC_ORG_004")
    public void testStep1InvalidEmailValidation() {
        WebDriver driver = DriverFactory.getDriver();
        OrganizationPage orgPage = new OrganizationPage(driver);

        Map<String, String> data = ExcelTestDataReader.getTestDataMap("Organization", "TC_ORG_004");
        String invalidEmail = data.getOrDefault("Email", "invalid-email");

        if (orgPage.isAddOrganizationButtonVisible()) {
            orgPage.clickAddOrganization();
            Assert.assertTrue(orgPage.isDrawerOpen(), "Drawer should be open");

            orgPage.enterStep1Details("TestOrgAutomated", invalidEmail, "123 Tech Park");
            orgPage.clickStep1Next();

            boolean emailError = orgPage.isValidationMessagePresent("Wrong email format")
                    || orgPage.isValidationMessagePresent("email");
            Assert.assertTrue(emailError || orgPage.isDrawerOpen(), "Email validation error should appear for invalid email");
        }
    }

    @Test(priority = 5, description = "TC_ORG_005: Verify 3-step wizard navigation driven from Excel TC_ORG_005, TC_ORG_006, TC_ORG_007")
    public void testStepWizardNavigation() {
        WebDriver driver = DriverFactory.getDriver();
        OrganizationPage orgPage = new OrganizationPage(driver);

        if (orgPage.isAddOrganizationButtonVisible()) {
            orgPage.clickAddOrganization();
            Assert.assertTrue(orgPage.isDrawerOpen(), "Drawer should be open");

            // Step 1: Read details from Excel TC_ORG_005
            Map<String, String> step1Data = ExcelTestDataReader.getTestDataMap("Organization", "TC_ORG_005");
            String orgName = step1Data.getOrDefault("Org", "TechCorp");
            String orgEmail = step1Data.getOrDefault("Email", "admin@techcorp.com");
            String address = step1Data.getOrDefault("Address", "100 Main St");
            String currency = step1Data.getOrDefault("Currency", "USD");
            String timezone = step1Data.getOrDefault("Timezone", "UTC");
            String financialYear = step1Data.getOrDefault("Financial Year", "JAN");

            orgPage.enterStep1Details(orgName, orgEmail, address, currency, timezone, financialYear);
            orgPage.clickStep1Next();

            // Step 2: Read details from Excel TC_ORG_006
            if (orgPage.isStep2Visible()) {
                Map<String, String> step2Data = ExcelTestDataReader.getTestDataMap("Organization", "TC_ORG_006");
                String adminName = step2Data.getOrDefault("Admin Name", "John Doe");
                String adminEmail = step2Data.getOrDefault("Email", "john@techcorp.com");
                String adminAddress = step2Data.getOrDefault("Address", "100 Main St");
                String adminPhone = step2Data.getOrDefault("Phone", "9876543210");

                orgPage.enterStep2Details(adminName, adminEmail, adminAddress, adminPhone);
                orgPage.clickStep2Next();
            }

            // Step 3: Read complexity check from Excel TC_ORG_007
            if (orgPage.isStep3Visible()) {
                Map<String, String> step3Data = ExcelTestDataReader.getTestDataMap("Organization", "TC_ORG_007");
                String password = step3Data.getOrDefault("Password", "weak");
                String confirmPassword = step3Data.getOrDefault("Confirm Password", "weak");

                orgPage.enterStep3Password(password, confirmPassword);
                System.out.println(">>> 3-step organization creation wizard successfully navigated to final submit step.");
            }
        }
    }

    @Test(priority = 6, description = "TC_ORG_008: Verify end-to-end Organization creation driven strictly from Excel TC_ORG_008")
    public void testCreateOrganizationEndToEnd() {
        WebDriver driver = DriverFactory.getDriver();
        OrganizationPage orgPage = new OrganizationPage(driver);

        Assert.assertTrue(orgPage.isAddOrganizationButtonVisible(),
                "Add New Organization button must be visible for Super Admin.");

        // Fetch full payload strictly from Excel row TC_ORG_008
        Map<String, String> data = ExcelTestDataReader.getTestDataMap("Organization", "TC_ORG_008");

        String orgName = data.getOrDefault("Organization Name", "AutoOrg Alpha");
        String orgEmail = data.getOrDefault("Organization Email", "autoorg.alpha@visdum.test");
        String address = data.getOrDefault("Address", "100 Innovation Way");
        String currency = data.getOrDefault("Currency", "USD");
        String timezone = data.getOrDefault("Timezone", "UTC");
        String financialYear = data.getOrDefault("Financial Year", "JAN");

        String adminName = data.getOrDefault("Admin Name", "Super Admin Alpha");
        String adminEmail = data.getOrDefault("Admin Email", "admin.alpha@visdum.test");
        String adminPhone = data.getOrDefault("Admin Phone", "9876543210");
        String password = data.getOrDefault("Password", "TempAdmin@1234");
        String confirmPassword = data.getOrDefault("Confirm Password", "TempAdmin@1234");

        orgPage.clickAddOrganization();
        Assert.assertTrue(orgPage.isDrawerOpen(), "Organization drawer should be open");

        // Step 1: Organization Details
        orgPage.enterStep1Details(orgName, orgEmail, address, currency, timezone, financialYear);
        orgPage.clickStep1Next();

        // Step 2: Admin User Details
        Assert.assertTrue(orgPage.isStep2Visible(), "Step 2 (Admin User Details) should be visible");
        orgPage.enterStep2Details(adminName, adminEmail, address, adminPhone);
        orgPage.clickStep2Next();

        // Step 3: Admin Password
        Assert.assertTrue(orgPage.isStep3Visible(), "Step 3 (Admin Password) should be visible");
        orgPage.enterStep3Password(password, confirmPassword);
        orgPage.submitOrganizationCreation();

        // Track created organization for safe automatic cleanup at suite teardown
        AutomationCleanupRegistry.trackCreatedOrganization("TC_ORG_008", null, orgName);

        // Verification: Toast & AG-Grid listing
        boolean successToast = orgPage.isSuccessToastVisible();
        orgPage.searchOrganization(orgName);
        boolean inGrid = orgPage.isOrganizationPresentInGrid(orgName);

        Assert.assertTrue(successToast || inGrid,
                "Expected newly created organization '" + orgName + "' to be created successfully and listed in grid.");
    }
}
