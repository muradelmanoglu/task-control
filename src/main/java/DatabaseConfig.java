import java.sql.*;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        
        if (dbUrl == null || dbUrl.isEmpty()) {
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/muradelmanoglu", "muradelmanoglu", "");
        }

        if (dbUrl.startsWith("postgres://")) {
            dbUrl = dbUrl.replace("postgres://", "jdbc:postgresql://");
        }
        
        if (!dbUrl.contains("sslmode")) {
            dbUrl += (dbUrl.contains("?") ? "&" : "?") + "sslmode=require";
        }

        return DriverManager.getConnection(dbUrl);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "nickname TEXT PRIMARY KEY, " +
                    "full_name TEXT, " +
                    "password TEXT, " +
                    "role TEXT)");

            // 2. Admin account (Orxan)
            String checkAdmin = "SELECT count(*) FROM users WHERE nickname = 'orxan'";
            ResultSet rs = stmt.executeQuery(checkAdmin);
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (nickname, full_name, password, role) " +
                             "VALUES ('orxan', 'Orxan', '12345', 'ADMIN')");
                System.out.println("✅ Admin (Orxan) bazaya əlavə edildi!");
            }

            // 3. Tasks table
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id TEXT PRIMARY KEY, " +
                    "owner_nickname TEXT REFERENCES users(nickname), " +
                    "user_fullname TEXT, " +
                    "title TEXT, " +
                    "lesson TEXT, " +
                    "status TEXT, " +
                    "file_name TEXT, " +
                    "updated_at TEXT)");

            System.out.println("✅ Verilənlər bazası hazırdır!");
        } catch (SQLException e) {
            System.err.println("❌ Baza xətası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
