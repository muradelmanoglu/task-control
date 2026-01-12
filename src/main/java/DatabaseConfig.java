import java.sql.*;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/muradelmanoglu", "muradelmanoglu", "");
        }
        
        // Render üçün mütləq formatlama
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
            // Cədvəlləri yarat
            stmt.execute("CREATE TABLE IF NOT EXISTS users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id TEXT PRIMARY KEY, owner_nickname TEXT REFERENCES users(nickname), user_fullname TEXT, title TEXT, lesson TEXT, status TEXT, file_name TEXT, updated_at TEXT)");
            
            // Admin Orxan-ı mütləq yenilə/yarat
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan', '12345', 'ADMIN') " +
                    "ON CONFLICT (nickname) DO UPDATE SET password = '12345', role = 'ADMIN'")) {
                ps.executeUpdate();
            }

            System.out.println("✅ Baza və Admin (orxan/12345) hazırdır!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
