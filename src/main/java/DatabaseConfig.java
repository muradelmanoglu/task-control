import java.sql.*;
import java.net.URI;
import java.net.URISyntaxException;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        
        if (dbUrl == null || dbUrl.isEmpty()) {
            // Lokal mühit üçün
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/muradelmanoglu", "muradelmanoglu", "");
        }

        // Render URL formatını JDBC-yə çeviririk
        try {
            if (dbUrl.startsWith("postgres://")) {
                dbUrl = dbUrl.replace("postgres://", "jdbc:postgresql://");
            }
            
            // SSL rejimini Render üçün mütləq edirik
            if (!dbUrl.contains("sslmode")) {
                dbUrl += (dbUrl.contains("?") ? "&" : "?") + "sslmode=require";
            }
            
            return DriverManager.getConnection(dbUrl);
        } catch (Exception e) {
            throw new SQLException("Bağlantı xətası: " + e.getMessage());
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // İstifadəçilər cədvəli
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "nickname TEXT PRIMARY KEY, " +
                    "full_name TEXT, " +
                    "password TEXT, " +
                    "role TEXT)");

            // Admin Orxan əlavə edilsin
            String checkAdmin = "SELECT count(*) FROM users WHERE nickname = 'orxan'";
            ResultSet rs = stmt.executeQuery(checkAdmin);
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan', '12345', 'ADMIN')");
            }

            // Tapşırıqlar cədvəli
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id TEXT PRIMARY KEY, " +
                    "owner_nickname TEXT REFERENCES users(nickname), " +
                    "user_fullname TEXT, " +
                    "title TEXT, " +
                    "lesson TEXT, " +
                    "status TEXT, " +
                    "file_name TEXT, " +
                    "updated_at TEXT)");
            
            System.out.println("✅ Baza Render mühitində uğurla hazırlandı!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
