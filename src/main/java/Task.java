

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id; // Unikal ID
    private String ownerNickname;
    private String userFullName;
    private String title;
    private String lesson;
    private String status;
    private String fileName;
    private String updatedAt;
    private boolean edited = false;

    public Task(String ownerNickname, String userFullName, String title, String lesson, String fileName) {
        this.id = UUID.randomUUID().toString();
        this.ownerNickname = ownerNickname;
        this.userFullName = userFullName;
        this.title = title;
        this.lesson = lesson;
        this.fileName = fileName;
        this.status = "In Progress";
        this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getOwnerNickname() { return ownerNickname; }
    public String getUserFullName() { return userFullName; }
    public String getTitle() { return title; }
    public String getLesson() { return lesson; }
    public String getStatus() { return status; }
    public String getFileName() { return fileName; }
    public String getUpdatedAt() { return updatedAt; }
    public boolean isEdited() { return edited; }

    public void setStatus(String status) { this.status = status; }
    public void setTitle(String title) { this.title = title; this.edited = true; }
    public void setLesson(String lesson) { this.lesson = lesson; this.edited = true; }
    public void setFileName(String fileName) { this.fileName = fileName; this.edited = true; }
    public void setUpdatedAt(String time) { this.updatedAt = time; }
}