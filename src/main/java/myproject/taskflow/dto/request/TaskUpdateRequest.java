package myproject.taskflow.dto.request;

import myproject.taskflow.enums.TaskStatus;

import java.time.LocalDateTime;

public class TaskUpdateRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private Integer priority;
    private String category;
    private Long assignedToId;
    private Long teamId;
    private LocalDateTime deadline;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
