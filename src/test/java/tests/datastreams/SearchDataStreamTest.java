package tests.datastreams;

import Base.BaseTest;
import Base.DriverFactory;
import Pages.DashboardPage;
import Pages.DataStreamsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SearchDataStreamTest extends BaseTest {

    @Test
    public void verifyDataStreamsPageLoadAndSearch() {
        DashboardPage dashboard = new DashboardPage(DriverFactory.getDriver());
        DataStreamsPage dataStreams = new DataStreamsPage(DriverFactory.getDriver());

        dashboard.navigateToDataStreams();
        Assert.assertTrue(dataStreams.isLoaded(), "Expected Data Streams page to load.");

        dataStreams.search("Deals");
    }
}
