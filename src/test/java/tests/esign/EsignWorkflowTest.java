package tests.esign;

import Base.EsignBaseTest;
import Pages.EsignPage;
import Pages.SendEsignPage;
import Pages.WithdrawEsignPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EsignWorkflowTest extends EsignBaseTest {

    @Test(description = "Verify E-Sign dashboard loads with envelope tracking table")
    public void verifyEsignDashboardLoads() {
        EsignPage esignPage = new EsignPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(esignPage.isLoaded(), "E-Sign dashboard should load");
    }

    @Test(description = "Verify navigating to Send Envelope page and returning via Cancel")
    public void verifySendEnvelopePageNavigation() {
        EsignPage esignPage = new EsignPage(Base.DriverFactory.getDriver());
        esignPage.clickSendEnvelope();

        SendEsignPage sendPage = new SendEsignPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(sendPage.isLoaded(), "Send Envelope page should load");

        sendPage.clickCancel();
        Assert.assertTrue(esignPage.isLoaded(), "Should return to E-Sign dashboard after cancel");
    }

    @Test(description = "Verify navigating to Withdraw Envelope page and returning via Cancel")
    public void verifyWithdrawEnvelopePageNavigation() {
        EsignPage esignPage = new EsignPage(Base.DriverFactory.getDriver());
        esignPage.clickWithdrawEnvelope();

        WithdrawEsignPage withdrawPage = new WithdrawEsignPage(Base.DriverFactory.getDriver());
        Assert.assertTrue(withdrawPage.isLoaded(), "Withdraw Envelope page should load");

        withdrawPage.clickCancel();
        Assert.assertTrue(esignPage.isLoaded(), "Should return to E-Sign dashboard after cancel");
    }

    @Test(description = "Verify opening Compose Email modal and closing it")
    public void verifyComposeEmailModalWorkflow() {
        EsignPage esignPage = new EsignPage(Base.DriverFactory.getDriver());
        esignPage.openComposeEmailModal();

        esignPage.enterEmailTitle("Commission Agreement Notification");
        esignPage.clickRestoreEmailTemplate();
        esignPage.closeComposeEmailModal();

        Assert.assertTrue(esignPage.isLoaded(), "Should stay on E-Sign dashboard after closing modal");
    }
}
