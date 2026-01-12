import java.sql.*;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        // 1. PostgreSQL driver-ini məcburi şəkildə proqrama tanıtdırırıq
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver tapılmadı!");
        }

        String dbUrl = System.getenv("DB_URL");
        
        // Əgər lokalda yoxlayırsansa
        if (dbUrl == null || dbUrl.isEmpty()) {
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/muradelmanoglu", "muradelmanoglu", "");
        }

        // 2. ƏN VACİB HİSSƏ: Render-in verdiyi URL-i Java-nın başa düşdüyü formaya salırıq
        String jdbcUrl;
        if (dbUrl.startsWith("postgres://")) {
            jdbcUrl = dbUrl.replace("postgres://", "jdbc:postgresql://");
        } else {
            jdbcUrl = dbUrl;
        }

        // 3. SSL-i mütləq aktiv edirik
        if (!jdbcUrl.contains("sslmode")) {
            jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
        }

        System.out.println("Bağlanılan URL: " + jdbcUrl); // Logda görmək üçün
        return DriverManager.getConnection(jdbcUrl);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Cədvəllər
            stmt.execute("CREATE TABLE IF NOT EXISTS users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id TEXT PRIMARY KEY, owner_nickname TEXT REFERENCES users(nickname), user_fullname TEXT, title TEXT, lesson TEXT, status TEXT, file_name TEXT, updated_at TEXT)");
            
            // Admin Orxan (Həmişə işləməsi üçün)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan', '12345', 'ADMIN') " +
                    "ON CONFLICT (nickname) DO UPDATE SET password = '12345', role = 'ADMIN'")) {
                ps.executeUpdate();
            }

            System.out.println("✅ Verilənlər bazası və Admin hazırdır!");
        } catch (SQLException e) {
            System.err.println("❌ Baza başlanğıc xətası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
