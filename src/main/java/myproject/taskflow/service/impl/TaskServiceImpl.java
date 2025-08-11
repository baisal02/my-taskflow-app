package myproject.taskflow.service.impl;

import myproject.taskflow.dto.request.TaskFilter;
import myproject.taskflow.dto.request.TaskRequest;
import myproject.taskflow.dto.request.TaskUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.TaskResponse;
import myproject.taskflow.entities.Task;
import myproject.taskflow.entities.User;
import myproject.taskflow.enums.TaskStatus;
import myproject.taskflow.repositories.jpa.TaskRepository;
import myproject.taskflow.repositories.jpa.TeamRepository;
import myproject.taskflow.repositories.jpa.UserRepository;
import myproject.taskflow.service.TaskService;
import myproject.taskflow.specification.TaskSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository, UserRepository userRepository1, TeamRepository teamRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository1;
        this.teamRepository = teamRepository;
    }

    @Override
    public TaskResponse createTask(TaskRequest taskRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User createdByUser = userRepository.findUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        User assignedTo = userRepository.findById(taskRequest.getAssigned_to())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setPriority(taskRequest.getPriority());
        task.setStatus(TaskStatus.NEW);
        task.setCategory(taskRequest.getCategory());
        task.setDeadline(taskRequest.getDeadline());
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdByUser);

        Task savedTask = taskRepository.save(task);

        return new TaskResponse(
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getDescription(),
                savedTask.getStatus(),
                savedTask.getPriority(),
                savedTask.getCategory(),
                savedTask.getDeadline(),
                savedTask.getAssignedTo().getId(),
                savedTask.getCreatedAt()
        );
    }


    @Override
    public Page<TaskResponse> getTasks(TaskFilter filter, Pageable pageable) {
        TaskSpecification specification = new TaskSpecification(filter);
        Page<Task> tasks = taskRepository.findAll(specification,pageable);
        return tasks.map(task -> new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCategory(),
                task.getDeadline(),
                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
                task.getCreatedAt()
        ));
    }

    @Override
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCategory(),
                task.getDeadline(),
                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
                task.getCreatedAt()
        );
    }

    @Override
    public SimpleResponse updateTask(Long id, TaskUpdateRequest taskUpdateRequest) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = currentUser.getId().equals(task.getCreatedBy().getId());

        boolean isAssigned = task.getAssignedTo() != null && currentUser.getId().equals(task.getAssignedTo().getId());

        boolean isManagerOrHigher = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGER") || auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAssigned && !isManagerOrHigher) {
            throw new AccessDeniedException("You don't have permission to manage this task");
        }

        User assignedUser = userRepository.findById(taskUpdateRequest.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        task.setTitle(taskUpdateRequest.getTitle());
        task.setDescription(taskUpdateRequest.getDescription());
        task.setStatus(taskUpdateRequest.getStatus());
        task.setPriority(taskUpdateRequest.getPriority());
        task.setCategory(taskUpdateRequest.getCategory());
        task.setAssignedTo(assignedUser);
        task.setDeadline(taskUpdateRequest.getDeadline());

        taskRepository.save(task);

        return new SimpleResponse("Updated successfully!", HttpStatus.OK);
    }


    @Override
    public SimpleResponse deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = currentUser.getId().equals(task.getCreatedBy().getId());
        boolean isManagerOrAdmin = currentUser.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")|| auth.getAuthority().equals("ROLE_MANAGER"));
        if (!isOwner && !isManagerOrAdmin) {
            throw new AccessDeniedException("You don't have permission to manage this task");
        }

        taskRepository.delete(task);
        return new SimpleResponse("Successfully Deleted!", HttpStatus.OK);
    }

    @Override
    public SimpleResponse changeTaskStatus(Long id, TaskStatus taskStatus) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = currentUser.getId().equals(task.getCreatedBy().getId());
        boolean isAssigned = task.getAssignedTo() != null && currentUser.getId().equals(task.getAssignedTo().getId());
        boolean isManagerOrHigher = currentUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER") || authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAssigned && !isManagerOrHigher) {
            throw new AccessDeniedException("You don't have permission to manage this task");
        }

        task.setStatus(taskStatus);
        taskRepository.save(task);
        return new SimpleResponse("Status Successfully Changed!", HttpStatus.OK);
    }


    @Override
    public SimpleResponse assignTaskToAnotherPerson(Long taskId, Long studentId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = currentUser.getId().equals(task.getCreatedBy().getId());
        boolean isManagerOrAdmin = currentUser.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_ADMIN")||
                        authority.getAuthority().equals("ROLE_MANAGER"));
        if(!isOwner && !isManagerOrAdmin) {
            throw new AccessDeniedException("You don't have permission to manage this task");
        }

        User assignedUser = userRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));
        task.setAssignedTo(assignedUser);
        taskRepository.save(task);
        return new SimpleResponse("Task Successfully reasigned",HttpStatus.OK);
    }
}
