package com.muradelmanoglu;

import java.sql.*;
import java.net.URI;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null && !dbUrl.isEmpty()) {
            try {
                URI dbUri = new URI(dbUrl);
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String dbFullUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
                Class.forName("org.postgresql.Driver");
                return DriverManager.getConnection(dbFullUrl, username, password);
            } catch (Exception e) { throw new SQLException(e); }
        }
        // Local: codepolis_db, istifadəçi: postgres, şifrə yoxdur
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/codepolis_db", "postgres", "");
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id TEXT PRIMARY KEY, owner_nickname TEXT REFERENCES users(nickname) ON DELETE CASCADE, user_fullname TEXT, title TEXT, lesson TEXT, status TEXT, file_name TEXT, updated_at TEXT)");

            // Admini yaradırıq (şifrəsi: 12345)
            stmt.execute("INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan Əlizadə', '12345', 'ADMIN') ON CONFLICT DO NOTHING");
            System.out.println("✅ Database hazırdır.");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}