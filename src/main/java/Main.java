import io.javalin.Javalin;
import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        DatabaseConfig.initializeDatabase();

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8081"));
        var app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(port);

        // --- LOGIN ---
        app.post("/api/login", ctx -> {
            try {
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String nick = body.get("nickname").toLowerCase().trim();
                String pass = body.get("password");

                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE nickname = ? AND password = ?")) {
                    pstmt.setString(1, nick);
                    pstmt.setString(2, pass);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("nickname", rs.getString("nickname"));
                        user.put("fullName", rs.getString("full_name"));
                        user.put("role", rs.getString("role"));
                        ctx.json(Map.of("success", true, "user", user));
                    } else {
                        ctx.json(Map.of("success", false, "message", "Məlumatlar yanlışdır!"));
                    }
                }
            } catch (Exception e) {
                ctx.status(500).json(Map.of("success", false, "message", "Server xətası!"));
            }
        });

        // --- REGISTER ---
        app.post("/api/register", ctx -> {
            try {
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String nick = body.get("nickname").toLowerCase().trim();
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users VALUES (?, ?, ?, ?)")) {
                    pstmt.setString(1, nick);
                    pstmt.setString(2, body.get("fullName"));
                    pstmt.setString(3, body.get("password"));
                    pstmt.setString(4, "STUDENT");
                    pstmt.executeUpdate();
                    ctx.json(Map.of("success", true));
                }
            } catch (SQLException e) {
                ctx.json(Map.of("success", false, "message", "Bu nikneym artıq mövcuddur!"));
            }
        });

        // --- FETCH ALL USERS ---
        app.get("/api/users", ctx -> {
            List<Map<String, String>> users = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                while (rs.next()) {
                    users.add(Map.of("nickname", rs.getString("nickname"),
                            "fullName", rs.getString("full_name"),
                            "role", rs.getString("role")));
                }
                ctx.json(users);
            }
        });

        // --- TASKS: GET ---
        app.get("/api/tasks", ctx -> {
            List<Map<String, String>> tasks = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM tasks ORDER BY updated_at DESC")) {
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

        // --- TASKS: ADD ---
        app.post("/api/tasks/add", ctx -> {
            Map<String, String> t = ctx.bodyAsClass(Map.class);
            String id = UUID.randomUUID().toString();
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO tasks VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                pstmt.setString(1, id);
                pstmt.setString(2, t.get("ownerNickname"));
                pstmt.setString(3, t.get("userFullName"));
                pstmt.setString(4, t.get("title"));
                pstmt.setString(5, t.get("lesson"));
                pstmt.setString(6, "In Progress");
                pstmt.setString(7, t.get("fileName"));
                pstmt.setString(8, time);
                pstmt.executeUpdate();
                ctx.status(201);
            }
        });

        // --- TASKS: UPDATE STATUS ---
        app.post("/api/tasks/status", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE tasks SET status = ? WHERE id = ?")) {
                pstmt.setString(1, body.get("status"));
                pstmt.setString(2, body.get("id"));
                pstmt.executeUpdate();
                ctx.json(Map.of("success", true));
            }
        });

        // --- TASKS: DELETE ---
        app.post("/api/tasks/delete", ctx -> {
            String id = ctx.queryParam("id");
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
                ctx.json(Map.of("success", true));
            }
        });
    }
}
