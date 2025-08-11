package myproject.taskflow.dto.request;

import java.time.LocalDateTime;

public class TaskRequest {
    private String title;
    private String description;
    private Integer priority;
    private String category;
    private LocalDateTime deadline;
    private Long assigned_to;
    private Long team_id;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public Long getAssigned_to() {
        return assigned_to;
    }

    public Long getTeam_id() {
        return team_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public void setAssigned_to(Long assigned_to) {
        this.assigned_to = assigned_to;
    }

    public void setTeam_id(Long team_id) {
        this.team_id = team_id;
    }
}
