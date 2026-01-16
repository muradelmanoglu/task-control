package com.muradelmanoglu;

import io.javalin.Javalin;
import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        DatabaseConfig.initializeDatabase();

        var app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(Integer.parseInt(System.getenv().getOrDefault("PORT", "8082")));

        // --- LOGIN ---
        app.post("/api/login", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String nick = body.get("nickname").trim().toLowerCase();
            String pass = body.get("password");

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE nickname = ? AND password = ?")) {
                pstmt.setString(1, nick);
                pstmt.setString(2, pass);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    ctx.json(Map.of("success", true, "user", Map.of(
                            "nickname", rs.getString("nickname"),
                            "fullName", rs.getString("full_name"),
                            "role", rs.getString("role")
                    )));
                } else { ctx.json(Map.of("success", false, "message", "Məlumatlar yanlışdır!")); }
            }
        });

        // --- REGISTER ---
        app.post("/api/register", ctx -> {
            Map<String, String> b = ctx.bodyAsClass(Map.class);
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO users VALUES (?, ?, ?, ?)")) {
                ps.setString(1, b.get("nickname").toLowerCase().trim());
                ps.setString(2, b.get("fullName"));
                ps.setString(3, b.get("password"));
                ps.setString(4, "STUDENT");
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            } catch (Exception e) { ctx.json(Map.of("success", false)); }
        });

        // --- ADMIN: USERS LIST & DELETE ---
        app.get("/api/users", ctx -> {
            List<Map<String, String>> users = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM users")) {
                while (rs.next()) {
                    users.add(Map.of("nickname", rs.getString("nickname"), "fullName", rs.getString("full_name"), "role", rs.getString("role")));
                }
                ctx.json(users);
            }
        });

        app.post("/api/users/delete", ctx -> {
            String nick = ctx.queryParam("nickname");
            if ("orxan".equals(nick)) return;
            try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE nickname = ?")) {
                ps.setString(1, nick);
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            }
        });

        // --- TASKS: GET & ADD ---
        app.get("/api/tasks", ctx -> {
            List<Map<String, String>> tasks = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM tasks ORDER BY updated_at DESC")) {
                while (rs.next()) {
                    Map<String, String> t = new HashMap<>();
                    t.put("id", rs.getString("id"));
                    t.put("ownerNickname", rs.getString("owner_nickname"));
                    t.put("userFullName", rs.getString("user_fullname"));
                    t.put("title", rs.getString("title"));
                    t.put("lesson", rs.getString("lesson"));
                    t.put("status", rs.getString("status"));
                    t.put("fileName", rs.getString("file_name"));
                    t.put("updatedAt", rs.getString("updated_at"));
                    tasks.add(t);
                }
                ctx.json(tasks);
            }
        });

        app.post("/api/tasks/add", ctx -> {
            Map<String, String> t = ctx.bodyAsClass(Map.class);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO tasks VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, t.get("ownerNickname"));
                ps.setString(3, t.get("userFullName"));
                ps.setString(4, t.get("title"));
                ps.setString(5, t.get("lesson"));
                ps.setString(6, "In Progress");
                ps.setString(7, t.get("fileName"));
                ps.setString(8, time);
                ps.executeUpdate();
                ctx.status(201).json(Map.of("success", true));
            } catch (Exception e) { e.printStackTrace(); ctx.status(500); }
        });

        // --- UPDATE STATUS & DELETE TASK ---
        app.post("/api/tasks/status", ctx -> {
            Map<String, String> b = ctx.bodyAsClass(Map.class);
            try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET status = ? WHERE id = ?")) {
                ps.setString(1, b.get("status"));
                ps.setString(2, b.get("id"));
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            }
        });

        app.post("/api/tasks/delete", ctx -> {
            try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
                ps.setString(1, ctx.queryParam("id"));
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            }
        });
    }
}