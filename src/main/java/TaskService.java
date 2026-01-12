
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskService {

    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (owner_nickname, user_fullname, title, lesson, status, file_name, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getOwnerNickname());
            pstmt.setString(2, task.getUserFullName());
            pstmt.setString(3, task.getTitle());
            pstmt.setString(4, task.getLesson());
            pstmt.setString(5, "In Progress");
            pstmt.setString(6, task.getFileName());
            pstmt.setString(7, task.getUpdatedAt());
            pstmt.executeUpdate();
            System.out.println("Task bazaya yazildi!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Task> getTasksByStudent(String nickname) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE owner_nickname = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Task(
                        rs.getString("owner_nickname"),
                        rs.getString("user_fullname"),
                        rs.getString("title"),
                        rs.getString("lesson"),
                        rs.getString("file_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}