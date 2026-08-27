package utilities;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
 
/**
 * Reads the latest OTP for 2FA automation from the UAT MySQL database.
 *
 * <p>Query: {@code SELECT two_factor_code FROM two_factor_authentications ORDER BY id DESC LIMIT 1}
 * (the newest row belongs to the login just triggered by the test). Connection settings come from
 * {@code config.properties}: {@code db.host}, {@code db.port}, {@code db.name}, {@code db.user},
 * {@code db.password}.
 */
public final class OtpDbReader {
 
    private OtpDbReader() {
    }
 
    public static String latestOtp() {
        String host = ConfigReader.get("db.host");
        String port = ConfigReader.get("db.port", "3306");
        String dbName = ConfigReader.get("db.name");
        String user = ConfigReader.get("db.user");
        String password = ConfigReader.get("db.password");
 
        if (host == null || host.trim().isEmpty()
                || dbName == null || dbName.trim().isEmpty()
                || user == null || user.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new IllegalStateException(
                    "DB config missing in src/test/resources/config.properties "
                            + "(db.host / db.name / db.user / db.password). OTP fetch cannot run.");
        }
 
        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String sql = "SELECT two_factor_code FROM two_factor_authentications ORDER BY id DESC LIMIT 1";
 
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String otp = rs.getString(1);
                if (otp == null || otp.trim().isEmpty()) {
                    throw new IllegalStateException("Latest two_factor_authentications row has an empty two_factor_code.");
                }
                return otp.trim();
            }
            throw new IllegalStateException("two_factor_authentications table is empty -- no OTP row found.");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch latest OTP from DB", e);
        }
    }
}