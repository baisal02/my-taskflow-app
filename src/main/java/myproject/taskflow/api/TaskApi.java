package myproject.taskflow.api;

import myproject.taskflow.dto.request.TaskFilter;
import myproject.taskflow.dto.request.TaskRequest;
import myproject.taskflow.dto.request.TaskUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.TaskResponse;
import myproject.taskflow.enums.TaskStatus;
import myproject.taskflow.service.TaskService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskApi {
    private final TaskService taskService;

    public TaskApi(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task with the details provided in the request body.")
    public TaskResponse createTask(@RequestBody TaskRequest taskRequest) {
        return taskService.createTask(taskRequest);
    }

    @GetMapping("/tasks/filter")
    @Operation(summary = "Get filtered tasks with pagination and sorting", description = "Retrieves a paginated list of tasks filtered by optional criteria such as status, priority, category, creator, assignee, and team.")
    public Page<TaskResponse> getTasksByFilter(
            @Parameter(description = "Filter criteria for tasks") @ModelAttribute TaskFilter filter,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of tasks per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return taskService.getTasks(filter, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID", description = "Retrieves detailed information about a task by its ID.")
    public TaskResponse getTaskById(@Parameter(description = "ID of the task to retrieve") @PathVariable Long id) {
        return taskService.getTask(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Updates the task identified by the given ID with new data provided in the request body.")
    public SimpleResponse updateTask(
            @Parameter(description = "ID of the task to update") @PathVariable Long id,
            @Parameter(description = "Updated task data") @RequestBody TaskUpdateRequest taskRequest
    ) {
        return taskService.updateTask(id, taskRequest);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Deletes the task identified by the given ID.")
    public SimpleResponse deleteTask(@Parameter(description = "ID of the task to delete") @PathVariable Long id) {
        return taskService.deleteTask(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change task status", description = "Updates the status of the task identified by the given ID.")
    public SimpleResponse changeStatus(
            @Parameter(description = "ID of the task") @PathVariable Long id,
            @Parameter(description = "New status of the task") @RequestParam TaskStatus status
    ) {
        return taskService.changeTaskStatus(id, status);
    }

    @PatchMapping("/{userId}/assign")
    @Operation(summary = "Assign task to another user", description = "Reassigns a task to a different user identified by userId.")
    public SimpleResponse assignTaskToUser(
            @Parameter(description = "ID of the user to assign the task to") @PathVariable Long userId,
            @Parameter(description = "ID of the task to be reassigned") @RequestParam Long taskId
    ) {
        return taskService.assignTaskToAnotherPerson(taskId, userId);
    }

}
