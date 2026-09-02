package tests.datastreams;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.CreateDataStreamPage;
import Pages.DashboardPage;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utilities.ExecutionGuard;

public class CreateDataStreamTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void requireCreatePermission() {
        ExecutionGuard.requireDestructiveTestsEnabled();
    }

    @Test
    public void createDataStreamWizardOpenAndFillStep1() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        wizard.enterStreamDetails("Deals Stream " + System.currentTimeMillis(), "Earning", "");
        wizard.clickNextStep();
    }

    @Test
    public void createDataStreamValidationEmptyName() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        // Click next without entering stream name
        wizard.clickNextStep();
        Assert.assertTrue(wizard.isFieldErrorVisible("Name") || wizard.isFieldErrorVisible("required") || wizard.isOpen(),
                "Validation message should appear or stay on Step 1 when Stream Name is empty.");
    }

    @Test
    public void createDataStreamWithKeyValueType() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());
        CreateDataStreamPage wizard = new CreateDataStreamPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        dataStreams.clickCreateDataStream();

        wizard.enterStreamDetails("Key-Value Stream " + System.currentTimeMillis(), "Key-Value", "");
        wizard.clickNextStep();
    }
}
