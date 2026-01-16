package com.muradelmanoglu;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        // Bazanı qur
        DatabaseConfig.initializeDatabase();

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(getHerokuPort());

        // LOGIN Proseduru
        app.post("/login", ctx -> {
            // İstifadəçi adını balaca hərflərlə və boşluqsuz götürürük
            String nickname = ctx.formParam("nickname") != null ? ctx.formParam("nickname").trim().toLowerCase() : "";
            String password = ctx.formParam("password") != null ? ctx.formParam("password").trim() : "";

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE LOWER(nickname) = ? AND password = ?")) {
                
                ps.setString(1, nickname);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    ctx.sessionAttribute("currentUser", rs.getString("nickname"));
                    ctx.sessionAttribute("role", rs.getString("role"));
                    ctx.redirect("/dashboard.html");
                } else {
                    // Xəta halında index-ə qaytar
                    ctx.redirect("/index.html?error=wrong");
                }
            } catch (SQLException e) {
                ctx.result("Baza xətası baş verdi: " + e.getMessage());
            }
        });

        // Çıxış (Logout)
        app.get("/logout", ctx -> {
            ctx.consumeSessionAttribute("currentUser");
            ctx.redirect("/index.html");
        });

        // Dashboard məlumatlarını gətirən endpoint (Nümunə)
        app.get("/api/tasks", ctx -> {
            String user = ctx.sessionAttribute("currentUser");
            if (user == null) {
                ctx.status(401);
                return;
            }
            // Burada tapşırıqları bazadan çəkib qaytara bilərsən
            ctx.json(new ArrayList<>()); 
        });
    }

    private static int getHerokuPort() {
        String port = System.getenv("PORT");
        return port != null ? Integer.parseInt(port) : 8081;
    }
}
