package myproject.taskflow.dto.response;

import myproject.taskflow.enums.TaskStatus;

import java.time.LocalDateTime;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Integer priority;
    private String category;
    private LocalDateTime deadline;
    private Long assigned_to;
    private Long team_id;
    private LocalDateTime created_at;

    public TaskResponse(Long id, String title, String description, TaskStatus status, Integer priority, String category, LocalDateTime deadline, Long assigned_to, LocalDateTime created_at) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.category = category;
        this.deadline = deadline;
        this.assigned_to = assigned_to;
        this.created_at = created_at;
    }

    public Long getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

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

    public LocalDateTime getCreated_at() {
        return created_at;
    }
}
