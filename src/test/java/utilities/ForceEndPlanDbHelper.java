package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JDBC Database Helper for ticket V20-1717: Force End Plan Workflow.
 * Queries live active and archived tables in test_visdum_app to perform
 * real-time assertions on EMT (Earnings), PMT (Payouts), CDC (Component Deal Credits),
 * Breakups, and Payout Summaries.
 */
public final class ForceEndPlanDbHelper {

    private ForceEndPlanDbHelper() {
    }

    public static Connection getConnection() throws SQLException {
        String host = ConfigReader.get("db.host");
        String port = ConfigReader.get("db.port", "3306");
        String dbName = ConfigReader.get("db.name");
        String user = ConfigReader.get("db.user");
        String password = ConfigReader.get("db.password");

        if (host == null || dbName == null || user == null || password == null) {
            throw new IllegalStateException("DB configuration parameters missing in config.properties");
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        return DriverManager.getConnection(url, user, password);
    }

    public static class EarningRecord {
        public long id;
        public int organizationId;
        public int planParticipantId;
        public String valueDate;
        public double earning;
        public String componentName;
        public String adjustmentType;

        @Override
        public String toString() {
            return String.format("EMT[id=%d, date=%s, earning=%.2f, component=%s, adj=%s]",
                    id, valueDate, earning, componentName, adjustmentType);
        }
    }

    public static class PayoutRecord {
        public long id;
        public int organizationId;
        public int planParticipantId;
        public String payoutDate;
        public double payout;
        public String approvalStatus;
        public String adjustmentType;
        public String payoutPeriod;

        @Override
        public String toString() {
            return String.format("PMT[id=%d, date=%s, payout=%.2f, status=%s, period=%s, adj=%s]",
                    id, payoutDate, payout, approvalStatus, payoutPeriod, adjustmentType);
        }
    }

    public static class ArchivedRecord {
        public long id;
        public long tableId;
        public int organizationId;
        public int planParticipantId;
        public String reason;
        public String deletedAt;
        public Long deletedBy;
    }

    public static class ForceEndImpactSummary {
        public int planParticipantId;
        public String expireDate;
        public List<EarningRecord> preservedEarnings = new ArrayList<>();
        public List<EarningRecord> archivedEarnings = new ArrayList<>();
        public List<PayoutRecord> preservedPayouts = new ArrayList<>();
        public List<PayoutRecord> archivedPayouts = new ArrayList<>();
        public Set<String> affectedPayoutPeriods = new HashSet<>();

        public double getPreservedEarningsSum() {
            return preservedEarnings.stream().mapToDouble(e -> e.earning).sum();
        }

        public double getArchivedEarningsSum() {
            return archivedEarnings.stream().mapToDouble(e -> e.earning).sum();
        }

        public double getPreservedPayoutsSum() {
            return preservedPayouts.stream().mapToDouble(p -> p.payout).sum();
        }

        public double getArchivedPayoutsSum() {
            return archivedPayouts.stream().mapToDouble(p -> p.payout).sum();
        }
    }

    /**
     * Fetches all active non-deleted EMT records for a participant.
     */
    public static List<EarningRecord> getActiveEarnings(int orgId, int planParticipantId) {
        String sql = "SELECT id, organization_id, plan_participant_id, value_date, earning, plan_component_name, adjustment_type "
                + "FROM data_stream_earning_master_transactions "
                + "WHERE organization_id = ? AND plan_participant_id = ? AND deleted_at IS NULL "
                + "ORDER BY value_date ASC, id ASC";

        List<EarningRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orgId);
            ps.setInt(2, planParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EarningRecord r = new EarningRecord();
                    r.id = rs.getLong("id");
                    r.organizationId = rs.getInt("organization_id");
                    r.planParticipantId = rs.getInt("plan_participant_id");
                    r.valueDate = rs.getString("value_date");
                    r.earning = rs.getDouble("earning");
                    r.componentName = rs.getString("plan_component_name");
                    r.adjustmentType = rs.getString("adjustment_type");
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query active earnings: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Fetches all active non-deleted PMT records for a participant.
     */
    public static List<PayoutRecord> getActivePayouts(int orgId, int planParticipantId) {
        String sql = "SELECT id, organization_id, plan_participant_id, payout_date, payout, approval_status, adjustment_type, payout_period "
                + "FROM data_stream_payout_master_transactions "
                + "WHERE organization_id = ? AND plan_participant_id = ? AND deleted_at IS NULL "
                + "ORDER BY payout_date ASC, id ASC";

        List<PayoutRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orgId);
            ps.setInt(2, planParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PayoutRecord r = new PayoutRecord();
                    r.id = rs.getLong("id");
                    r.organizationId = rs.getInt("organization_id");
                    r.planParticipantId = rs.getInt("plan_participant_id");
                    r.payoutDate = rs.getString("payout_date");
                    r.payout = rs.getDouble("payout");
                    r.approvalStatus = rs.getString("approval_status");
                    r.adjustmentType = rs.getString("adjustment_type");
                    r.payoutPeriod = rs.getString("payout_period");
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query active payouts: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Fetches all archived EMT records for a participant.
     */
    public static List<ArchivedRecord> getArchivedEarnings(int orgId, int planParticipantId) {
        String sql = "SELECT id, table_id, organization_id, plan_participant_id, reason, deleted_at, deleted_by "
                + "FROM archived_data_stream_earning_master_transactions "
                + "WHERE organization_id = ? AND plan_participant_id = ? "
                + "ORDER BY id DESC";

        List<ArchivedRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orgId);
            ps.setInt(2, planParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ArchivedRecord r = new ArchivedRecord();
                    r.id = rs.getLong("id");
                    r.tableId = rs.getLong("table_id");
                    r.organizationId = rs.getInt("organization_id");
                    r.planParticipantId = rs.getInt("plan_participant_id");
                    r.reason = rs.getString("reason");
                    r.deletedAt = rs.getString("deleted_at");
                    long delBy = rs.getLong("deleted_by");
                    r.deletedBy = rs.wasNull() ? null : delBy;
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query archived earnings: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Fetches all archived PMT records for a participant.
     */
    public static List<ArchivedRecord> getArchivedPayouts(int orgId, int planParticipantId) {
        String sql = "SELECT id, table_id, organization_id, plan_participant_id, reason, deleted_at, deleted_by "
                + "FROM archived_data_stream_payout_master_transactions "
                + "WHERE organization_id = ? AND plan_participant_id = ? "
                + "ORDER BY id DESC";

        List<ArchivedRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orgId);
            ps.setInt(2, planParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ArchivedRecord r = new ArchivedRecord();
                    r.id = rs.getLong("id");
                    r.tableId = rs.getLong("table_id");
                    r.organizationId = rs.getInt("organization_id");
                    r.planParticipantId = rs.getInt("plan_participant_id");
                    r.reason = rs.getString("reason");
                    r.deletedAt = rs.getString("deleted_at");
                    long delBy = rs.getLong("deleted_by");
                    r.deletedBy = rs.wasNull() ? null : delBy;
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query archived payouts: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Calculates the exact Force End Plan impact based on backend rules.
     * Rule 1: Earning with value_date <= expireDate -> Preserved
     * Rule 2: Earning with value_date > expireDate & adjustment_type IS NULL -> Archived & Deleted
     * Rule 3: Manual adjustments (adjustment_type IS NOT NULL) -> Always Preserved
     * Rule 4: Payout with approval_status == 'PAID' -> Always Preserved
     * Rule 5: Payout with payout_date > expireDate & approval_status != 'PAID' -> Archived & Deleted
     */
    public static ForceEndImpactSummary calculateImpact(int orgId, int planParticipantId, String expireDate) {
        ForceEndImpactSummary summary = new ForceEndImpactSummary();
        summary.planParticipantId = planParticipantId;
        summary.expireDate = expireDate;

        List<EarningRecord> earnings = getActiveEarnings(orgId, planParticipantId);
        List<PayoutRecord> payouts = getActivePayouts(orgId, planParticipantId);

        for (EarningRecord e : earnings) {
            if (e.adjustmentType != null) {
                // Manual adjustment always preserved
                summary.preservedEarnings.add(e);
            } else if (e.valueDate.compareTo(expireDate) <= 0) {
                summary.preservedEarnings.add(e);
            } else {
                summary.archivedEarnings.add(e);
            }
        }

        for (PayoutRecord p : payouts) {
            if ("PAID".equalsIgnoreCase(p.approvalStatus) || p.adjustmentType != null) {
                // Paid or adjustment payouts always preserved
                summary.preservedPayouts.add(p);
            } else if (p.payoutDate.compareTo(expireDate) <= 0) {
                summary.preservedPayouts.add(p);
            } else {
                summary.archivedPayouts.add(p);
                if (p.payoutPeriod != null && !p.payoutPeriod.isEmpty()) {
                    summary.affectedPayoutPeriods.add(p.payoutPeriod);
                }
            }
        }

        return summary;
    }

    /**
     * Verifies that the given table exists and contains the specified columns.
     */
    public static List<String> getTableColumns(String tableName) {
        String sql = "SELECT column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ?";
        List<String> columns = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    columns.add(rs.getString("column_name").toLowerCase());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query columns for table " + tableName + ": " + e.getMessage(), e);
        }
        return columns;
    }
}
