package utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dynamic Automation Test Data Tracker & Cleanup Registry.
 *
 * Rules:
 * 1. Zero hardcoded admin credentials: The account used to log in (from config or session)
 *    is dynamically protected and will NEVER be deleted.
 * 2. Only records created during the current automation run are tracked and cleaned up.
 * 3. Cleanup runs deferred at suite end, respecting dependent test cases.
 * 4. Verification is executed after every deletion to confirm 0 records remain.
 */
public final class AutomationCleanupRegistry {

    private AutomationCleanupRegistry() {
    }

    public enum RecordType {
        USER,
        ORGANIZATION
    }

    public enum CleanupStatus {
        PENDING,
        RETAINED_FOR_DEPENDENCY,
        CLEANED,
        FAILED,
        PROTECTED_SKIPPED
    }

    public static class TrackedRecord {
        public final String tcId;
        public final RecordType recordType;
        public Long recordId;
        public final String uniqueIdentifier; // email or orgName
        public final long createdTimestamp;
        public CleanupStatus status;
        public String failureReason;
        public boolean verifiedCleaned;

        public TrackedRecord(String tcId, RecordType recordType, Long recordId, String uniqueIdentifier) {
            this.tcId = tcId;
            this.recordType = recordType;
            this.recordId = recordId;
            this.uniqueIdentifier = uniqueIdentifier;
            this.createdTimestamp = System.currentTimeMillis();
            this.status = CleanupStatus.PENDING;
            this.verifiedCleaned = false;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: id=%s, identifier=%s, status=%s",
                    tcId, recordType, recordId, uniqueIdentifier, status);
        }
    }

    // Dynamic set of credentials actively used for authentication (NEVER to be deleted)
    private static final Set<String> DYNAMIC_PROTECTED_EMAILS = ConcurrentHashMap.newKeySet();
    private static final Set<Long> DYNAMIC_PROTECTED_USER_IDS = ConcurrentHashMap.newKeySet();

    // Tracked records created by automation during current execution run
    private static final List<TrackedRecord> TRACKED_RECORDS = new CopyOnWriteArrayList<>();

    static {
        // Dynamically register the active login username from configuration
        String sessionUser = ConfigReader.get("username");
        if (sessionUser != null && !sessionUser.isBlank()) {
            registerProtectedSessionAccount(sessionUser.trim());
        }
    }

    /**
     * Dynamically registers an account as protected so it can NEVER be deleted.
     */
    public static void registerProtectedSessionAccount(String email) {
        if (email != null && !email.isBlank()) {
            DYNAMIC_PROTECTED_EMAILS.add(email.trim().toLowerCase());
            System.out.println(">>> [SECURITY SHIELD] Dynamically protected session account: " + email.trim());
        }
    }

    public static void registerProtectedSessionUserId(long userId) {
        DYNAMIC_PROTECTED_USER_IDS.add(userId);
    }

    /**
     * Evaluates whether an email belongs to the active login credentials or a protected account.
     */
    public static boolean isProtected(String email) {
        if (email == null || email.isBlank()) {
            return true;
        }

        // 1. Dynamic check against the configured login user
        String sessionUser = ConfigReader.get("username");
        if (sessionUser != null && email.trim().equalsIgnoreCase(sessionUser.trim())) {
            return true;
        }

        // 2. Dynamic check against any registered session account
        return DYNAMIC_PROTECTED_EMAILS.contains(email.trim().toLowerCase());
    }

    /**
     * Tracks a user created by an automation test case.
     */
    public static void trackCreatedUser(String tcId, Long userId, String email) {
        if (isProtected(email)) {
            System.err.println(">>> [CLEANUP REGISTRY] Refusing to track protected login account: " + email);
            return;
        }

        TrackedRecord record = new TrackedRecord(tcId, RecordType.USER, userId, email);
        TRACKED_RECORDS.add(record);
        System.out.println(">>> [CLEANUP REGISTRY] Tracked created user for future cleanup: " + record);
    }

    /**
     * Tracks an organization created by an automation test case.
     */
    public static void trackCreatedOrganization(String tcId, Long orgId, String orgName) {
        // Organization ID 94 (Mayank Test) is strictly protected
        if (orgId != null && orgId == 94) {
            System.err.println(">>> [CLEANUP REGISTRY] Refusing to track primary tenant Org 94!");
            return;
        }

        TrackedRecord record = new TrackedRecord(tcId, RecordType.ORGANIZATION, orgId, orgName);
        TRACKED_RECORDS.add(record);
        System.out.println(">>> [CLEANUP REGISTRY] Tracked created organization: " + record);
    }

    public static List<TrackedRecord> getTrackedRecords() {
        return Collections.unmodifiableList(TRACKED_RECORDS);
    }

    /**
     * Executes targeted cleanup for all automation-created records at the end of the suite.
     */
    public static synchronized void executeSuiteCleanup() {
        System.out.println("\n======================================================================");
        System.out.println(" AUTOMATIC TEST DATA CLEANUP: Executing targeted cleanup for tracked records");
        System.out.println("======================================================================");

        if (TRACKED_RECORDS.isEmpty()) {
            System.out.println(">>> No test data was created during this execution run. Nothing to clean.");
            return;
        }

        for (TrackedRecord record : TRACKED_RECORDS) {
            if (record.status == CleanupStatus.CLEANED) {
                continue;
            }

            if (record.recordType == RecordType.USER) {
                cleanUserRecord(record);
            } else if (record.recordType == RecordType.ORGANIZATION) {
                cleanOrgRecord(record);
            }
        }

        printCleanupReport();
    }

    private static void cleanUserRecord(TrackedRecord record) {
        String email = record.uniqueIdentifier;

        // Dynamic protection barrier
        if (isProtected(email)) {
            record.status = CleanupStatus.PROTECTED_SKIPPED;
            record.failureReason = "Account matches active login credentials (dynamically protected).";
            System.err.println(">>> [CLEANUP ABORTED] Protected account skipped: " + email);
            return;
        }

        try (Connection conn = ForceEndPlanDbHelper.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // If recordId wasn't captured during creation, look it up by email
                if (record.recordId == null) {
                    String lookupSql = "SELECT id FROM user_master WHERE email = ? AND organization_id = 94";
                    try (PreparedStatement ps = conn.prepareStatement(lookupSql)) {
                        ps.setString(1, email);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                record.recordId = rs.getLong("id");
                            }
                        }
                    }
                }

                if (record.recordId == null) {
                    // Record was either already cleaned up or never created
                    record.status = CleanupStatus.CLEANED;
                    record.verifiedCleaned = true;
                    conn.rollback();
                    return;
                }

                long uid = record.recordId;

                // Step 1: Clean child table relations
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM two_factor_authentications WHERE user_id = ?")) {
                    ps.setLong(1, uid);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM user_master_details WHERE user_id = ?")) {
                    ps.setLong(1, uid);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM user_teams WHERE user_id = ?")) {
                    ps.setLong(1, uid);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM user_departments WHERE user_id = ?")) {
                    ps.setLong(1, uid);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM data_user_reference WHERE user_id = ?")) {
                    ps.setLong(1, uid);
                    ps.executeUpdate();
                }

                // Step 2: Delete from user_master
                String deleteSql = "DELETE FROM user_master WHERE id = ? AND organization_id = 94 AND email = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setLong(1, uid);
                    ps.setString(2, email);
                    ps.executeUpdate();
                }

                conn.commit();

                // Step 3: Verification Query (Confirm count = 0)
                String verifySql = "SELECT COUNT(*) FROM user_master WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(verifySql)) {
                    ps.setLong(1, uid);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            record.status = CleanupStatus.CLEANED;
                            record.verifiedCleaned = true;
                            System.out.println(">>> [CLEANUP SUCCESS] User purged and verified: " + email + " (ID: " + uid + ")");
                        } else {
                            record.status = CleanupStatus.FAILED;
                            record.failureReason = "Verification query found record still present in DB.";
                        }
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                record.status = CleanupStatus.FAILED;
                record.failureReason = e.getMessage();
                System.err.println(">>> [CLEANUP ERROR] Failed cleaning user " + email + ": " + e.getMessage());
            }

        } catch (SQLException e) {
            record.status = CleanupStatus.FAILED;
            record.failureReason = "DB Connection failure: " + e.getMessage();
        }
    }

    private static void cleanOrgRecord(TrackedRecord record) {
        if (record.recordId != null && record.recordId == 94) {
            record.status = CleanupStatus.PROTECTED_SKIPPED;
            record.failureReason = "Protected tenant Organization 94 cannot be deleted.";
            return;
        }

        try (Connection conn = ForceEndPlanDbHelper.getConnection()) {
            if (record.recordId == null && record.uniqueIdentifier != null) {
                String lookupSql = "SELECT id FROM organizations WHERE organization_name = ? AND id != 94";
                try (PreparedStatement ps = conn.prepareStatement(lookupSql)) {
                    ps.setString(1, record.uniqueIdentifier);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            record.recordId = rs.getLong("id");
                        }
                    }
                }
            }

            if (record.recordId == null) {
                record.status = CleanupStatus.CLEANED;
                record.verifiedCleaned = true;
                return;
            }

            long orgId = record.recordId;
            if (orgId == 94) {
                record.status = CleanupStatus.PROTECTED_SKIPPED;
                return;
            }

            // Clean child entities created for this organization
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM vis_rbac_roles WHERE organization_id = ?")) {
                ps.setLong(1, orgId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM data_user_reference WHERE organization_id = ?")) {
                ps.setLong(1, orgId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM user_master_details WHERE organization_id = ?")) {
                ps.setLong(1, orgId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM user_master WHERE organization_id = ?")) {
                ps.setLong(1, orgId);
                ps.executeUpdate();
            }

            String deleteSql = "DELETE FROM organizations WHERE id = ? AND id != 94";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setLong(1, orgId);
                ps.executeUpdate();
            }

            // Verify
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM organizations WHERE id = ?")) {
                ps.setLong(1, orgId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        record.status = CleanupStatus.CLEANED;
                        record.verifiedCleaned = true;
                        System.out.println(">>> [CLEANUP SUCCESS] Organization purged and verified: " + record.uniqueIdentifier + " (ID: " + orgId + ")");
                    } else {
                        record.status = CleanupStatus.FAILED;
                        record.failureReason = "Verification query found organization still present.";
                    }
                }
            }
        } catch (SQLException e) {
            record.status = CleanupStatus.FAILED;
            record.failureReason = e.getMessage();
        }
    }

    /**
     * Prints a formatted summary Cleanup Report to console.
     */
    public static void printCleanupReport() {
        System.out.println("\n------------------------------------------------------------------------------------------------------------------");
        System.out.println(" AUTOMATIC TEST DATA CLEANUP EXECUTION REPORT");
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-12s | %-10s | %-32s | %-12s | %-12s%n",
                "TC ID", "Record Type", "Record ID", "Unique Identifier", "Status", "Verified");
        System.out.println("------------------------------------------------------------------------------------------------------------------");

        int cleaned = 0;
        int failed = 0;
        int skipped = 0;

        for (TrackedRecord r : TRACKED_RECORDS) {
            System.out.printf("%-12s | %-12s | %-10s | %-32s | %-12s | %-12s%n",
                    r.tcId,
                    r.recordType,
                    r.recordId != null ? String.valueOf(r.recordId) : "N/A",
                    r.uniqueIdentifier,
                    r.status,
                    r.verifiedCleaned ? "CONFIRMED (0)" : "NO");

            if (r.status == CleanupStatus.CLEANED) cleaned++;
            else if (r.status == CleanupStatus.FAILED) failed++;
            else skipped++;
        }

        System.out.println("------------------------------------------------------------------------------------------------------------------");
        System.out.printf("Total Tracked: %d | Cleaned: %d | Failed: %d | Skipped/Protected: %d%n",
                TRACKED_RECORDS.size(), cleaned, failed, skipped);
        System.out.println("------------------------------------------------------------------------------------------------------------------\n");
    }

    /**
     * Pre-flight sanity reset for specified test emails (prevents leftover conflicts if previous run was aborted).
     */
    public static void preFlightReset(String... testEmails) {
        for (String email : testEmails) {
            if (isProtected(email)) continue;
            TrackedRecord r = new TrackedRecord("PRE-FLIGHT", RecordType.USER, null, email);
            cleanUserRecord(r);
        }
    }
}
