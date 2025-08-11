package myproject.taskflow.dto.request;

import myproject.taskflow.enums.TaskStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class TaskFilter {
    private TaskStatus status;
    private Integer priority;
    private String category;

    private Long createdById;
    private Long assignedToId;
    private Long teamId;

    private OffsetDateTime deadlineFrom;
    private OffsetDateTime  deadlineTo;
    private OffsetDateTime  createdAtFrom;
    private OffsetDateTime  createdAtTo;

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public OffsetDateTime getDeadlineFrom() {
        return deadlineFrom;
    }

    public OffsetDateTime getDeadlineTo() {
        return deadlineTo;
    }

    public OffsetDateTime getCreatedAtFrom() {
        return createdAtFrom;
    }

    public OffsetDateTime getCreatedAtTo() {
        return createdAtTo;
    }
}
