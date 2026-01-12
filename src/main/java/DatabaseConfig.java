
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null) {
            // Skrinşotda görünən məlumatlara əsasən:
            // Port: 5432, Database: muradelmanoglu, User: muradelmanoglu
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/muradelmanoglu", "muradelmanoglu", "");
        }
        return DriverManager.getConnection(dbUrl);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // İstifadəçilər cədvəli
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "nickname TEXT PRIMARY KEY, " +
                    "full_name TEXT, " +
                    "password TEXT, " +
                    "role TEXT)");

            // Tapşırıqlar cədvəli (id-ni serial və ya text edə bilərik, burada UUID/Text istifadə edirik)
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id TEXT PRIMARY KEY, " +
                    "owner_nickname TEXT REFERENCES users(nickname), " +
                    "user_fullname TEXT, " +
                    "title TEXT, " +
                    "lesson TEXT, " +
                    "status TEXT, " +
                    "file_name TEXT, " +
                    "updated_at TEXT)");

            System.out.println("✅ Verilənlər bazası və cədvəllər uğurla hazırlandı!");
        } catch (SQLException e) {
            System.err.println("❌ Baza xətası: " + e.getMessage());
        }
    }
}