package myproject.taskflow.service;

import myproject.taskflow.dto.request.TaskFilter;
import myproject.taskflow.dto.request.TaskRequest;
import myproject.taskflow.dto.request.TaskUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.TaskResponse;
import myproject.taskflow.enums.TaskStatus;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface TaskService {
    TaskResponse createTask(TaskRequest taskRequest);
    Page<TaskResponse> getTasks(TaskFilter filter, Pageable pageable);
    TaskResponse getTask(Long id);
    SimpleResponse updateTask(Long id, TaskUpdateRequest taskUpdateRequest);
    SimpleResponse deleteTask(Long id);
    SimpleResponse changeTaskStatus(Long id, TaskStatus taskStatus);
    SimpleResponse assignTaskToAnotherPerson(Long taskId, Long studentId);
}
