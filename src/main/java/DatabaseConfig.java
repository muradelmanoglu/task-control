package com.muradelmanoglu;

import java.sql.*;
import java.net.URI;
import java.net.URISyntaxException;

public class DatabaseConfig {

    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new SQLException("DB_URL environment variable is missing!");
        }

        try {
            URI dbUri = new URI(dbUrl);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbFullUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
            
            // PostgreSQL drayverini əllə yükləyirik
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(dbFullUrl, username, password);
        } catch (URISyntaxException | ClassNotFoundException e) {
            throw new SQLException("Database connection error: " + e.getMessage());
        }
    }

    public static void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "nickname TEXT PRIMARY KEY, " +
                "full_name TEXT, " +
                "password TEXT, " +
                "role TEXT)";

        String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id TEXT PRIMARY KEY, " +
                "owner_nickname TEXT REFERENCES users(nickname), " +
                "user_fullname TEXT, " +
                "title TEXT, " +
                "lesson TEXT, " +
                "status TEXT, " +
                "file_name TEXT, " +
                "updated_at TEXT)";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createTasksTable);

            // Admin istifadəçisini təmizləyib yenidən yaradırıq (şifrənin 12345 olduğundan əmin olmaq üçün)
            stmt.execute("DELETE FROM users WHERE nickname = 'orxan'");
            stmt.execute("INSERT INTO users (nickname, full_name, password, role) " +
                         "VALUES ('orxan', 'Orxan', '12345', 'ADMIN')");

            System.out.println("✅ Verilənlər bazası və Admin 'orxan' (12345) hazırdır!");
        } catch (SQLException e) {
            System.err.println("❌ Database init error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
