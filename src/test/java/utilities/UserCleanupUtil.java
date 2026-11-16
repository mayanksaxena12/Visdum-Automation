package utilities;

/**
 * Delegating Utility for Post-Test Cleanup.
 * Zero hardcoded credentials: All protection rules are dynamically evaluated
 * against the actively authenticated login session via AutomationCleanupRegistry.
 */
public final class UserCleanupUtil {

    private UserCleanupUtil() {
    }

    /**
     * Purges a test user by email from the UAT tenant database.
     * Evaluates dynamic protection against active session credentials.
     */
    public static boolean cleanupUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        if (AutomationCleanupRegistry.isProtected(email)) {
            System.err.println(">>> [SECURITY BARRIER] Refusing to cleanup dynamically protected session account: " + email);
            return false;
        }

        AutomationCleanupRegistry.TrackedRecord record = new AutomationCleanupRegistry.TrackedRecord(
                "MANUAL_CLEANUP", AutomationCleanupRegistry.RecordType.USER, null, email.trim()
        );
        AutomationCleanupRegistry.trackCreatedUser("CLEANUP_REQ", null, email.trim());
        AutomationCleanupRegistry.executeSuiteCleanup();
        return true;
    }

    /**
     * Purges a test user by User Reference ID.
     */
    public static boolean cleanupUserByRefId(String refId) {
        if (refId == null || refId.isBlank()) {
            return false;
        }

        String sql = "DELETE FROM data_user_reference WHERE user_reference_id = ? AND organization_id = 94";

        try (java.sql.Connection conn = ForceEndPlanDbHelper.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, refId.trim());
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                System.out.println(">>> [AUTO-CLEANUP] Successfully purged test user with Ref ID: " + refId);
                return true;
            }
        } catch (java.sql.SQLException e) {
            System.err.println(">>> [AUTO-CLEANUP] Warning: Could not purge user by Ref ID " + refId + ": " + e.getMessage());
        }
        return false;
    }
}
