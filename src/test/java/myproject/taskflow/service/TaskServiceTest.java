package myproject.taskflow.service;

import myproject.taskflow.dto.request.RegisterRequest;
import myproject.taskflow.dto.request.TaskRequest;
import myproject.taskflow.dto.request.TaskUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.TaskResponse;
import myproject.taskflow.entities.Task;
import myproject.taskflow.entities.User;
import myproject.taskflow.enums.Role;
import myproject.taskflow.enums.TaskStatus;
import myproject.taskflow.repositories.jpa.TaskRepository;
import myproject.taskflow.repositories.jpa.TeamRepository;
import myproject.taskflow.repositories.jpa.UserRepository;
import myproject.taskflow.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User createUser(Long id, String email, String roleName) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(Role.valueOf(roleName));
        return user;
    }

    private Task createTask(Long id, User creator, User assignedTo) {
        Task task = new Task();
        task.setId(id);
        task.setCreatedBy(creator);
        task.setAssignedTo(assignedTo);
        task.setStatus(TaskStatus.NEW);
        task.setPriority(3);
        task.setCategory("category");
        task.setDeadline(LocalDateTime.now().plusDays(1));
        return task;
    }

    private void mockSecurityContext(String email) {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createTask_success() {
        RegisterRequest req = new RegisterRequest("nick", "email@test.com", "pass", "John", "Doe");
        // setup user who creates task
        User creator = createUser(1L, "creator@test.com", "USER");
        User assigned = createUser(2L, "assigned@test.com", "USER");

        mockSecurityContext("creator@test.com");

        when(userRepository.findUserByEmail("creator@test.com")).thenReturn(Optional.of(creator));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assigned));

        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("title");
        taskRequest.setDescription("desc");
        taskRequest.setPriority(2);
        taskRequest.setCategory("cat");
        taskRequest.setDeadline(LocalDateTime.now().plusDays(2));
        taskRequest.setAssigned_to(2L);

        Task savedTask = createTask(10L, creator, assigned);
        savedTask.setTitle("title");
        savedTask.setDescription("desc");
        savedTask.setPriority(2);
        savedTask.setCategory("cat");
        savedTask.setDeadline(taskRequest.getDeadline());

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(taskRequest);

        assertNotNull(response);
        assertEquals(savedTask.getId(), response.getId());
        assertEquals(savedTask.getTitle(), response.getTitle());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_byOwner_success() {
        User owner = createUser(1L, "owner@test.com", "USER");
        User assigned = createUser(2L, "assigned@test.com", "USER");
        User updater = owner; // updater - владелец задачи

        Task task = createTask(100L, owner, assigned);

        mockSecurityContext("owner@test.com");

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(userRepository.findUserByEmail("owner@test.com")).thenReturn(Optional.of(updater));

        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("new title");
        updateRequest.setDescription("new desc");
        updateRequest.setPriority(4);
        updateRequest.setCategory("new cat");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setAssignedToId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(assigned));
        when(taskRepository.save(task)).thenReturn(task);

        SimpleResponse response = taskService.updateTask(100L, updateRequest);

        assertEquals("Updated successfully!", response.getMessage());
        verify(taskRepository).save(task);
        assertEquals("new title", task.getTitle());
    }

    @Test
    void updateTask_byNonOwnerButAssignedUser_success() {
        User owner = createUser(1L, "owner@test.com", "USER");
        User assigned = createUser(2L, "assigned@test.com", "USER");
        User updater = assigned; // updater - назначенный пользователь

        Task task = createTask(100L, owner, assigned);

        mockSecurityContext("assigned@test.com");

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(userRepository.findUserByEmail("assigned@test.com")).thenReturn(Optional.of(updater));

        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("updated title");
        updateRequest.setDescription("updated desc");
        updateRequest.setPriority(5);
        updateRequest.setCategory("cat");
        updateRequest.setStatus(TaskStatus.REVIEW);
        updateRequest.setAssignedToId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(assigned));
        when(taskRepository.save(task)).thenReturn(task);

        SimpleResponse response = taskService.updateTask(100L, updateRequest);

        assertEquals("Updated successfully!", response.getMessage());
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_byManager_success() {
        User owner = createUser(1L, "owner@test.com", "USER");
        User assigned = createUser(2L, "assigned@test.com", "USER");
        User manager = createUser(3L, "manager@test.com", "MANAGER"); // роль менеджера

        Task task = createTask(100L, owner, assigned);

        mockSecurityContext("manager@test.com");

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(userRepository.findUserByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("manager updated");
        updateRequest.setDescription("desc");
        updateRequest.setPriority(3);
        updateRequest.setCategory("cat");
        updateRequest.setStatus(TaskStatus.COMPLETED);
        updateRequest.setAssignedToId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(assigned));
        when(taskRepository.save(task)).thenReturn(task);

        SimpleResponse response = taskService.updateTask(100L, updateRequest);

        assertEquals("Updated successfully!", response.getMessage());
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_byUnauthorizedUser_fail() {
        // подготовка данных — без изменений
        User owner = createUser(1L, "owner@test.com", "USER");
        User assigned = createUser(2L, "assigned@test.com", "USER");
        User stranger = createUser(4L, "stranger@test.com", "USER");

        Task task = createTask(100L, owner, assigned);

        mockSecurityContext("stranger@test.com");

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(userRepository.findUserByEmail("stranger@test.com")).thenReturn(Optional.of(stranger));

        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("should not update");
        updateRequest.setDescription("desc");
        updateRequest.setPriority(3);
        updateRequest.setCategory("cat");
        updateRequest.setStatus(TaskStatus.COMPLETED);
        updateRequest.setAssignedToId(2L);

        // проверяем, что выбрасывается исключение AccessDeniedException
        assertThrows(AccessDeniedException.class, () -> {
            taskService.updateTask(100L, updateRequest);
        });

        verify(taskRepository, never()).save(any());
    }


    @Test
    void deleteTask_byOwner_success() {
        User owner = createUser(1L, "owner@test.com", "USER");
        Task task = createTask(200L, owner, null);

        mockSecurityContext("owner@test.com");

        when(taskRepository.findById(200L)).thenReturn(Optional.of(task));
        when(userRepository.findUserByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        SimpleResponse response = taskService.deleteTask(200L);

        assertEquals("Successfully Deleted!", response.getMessage());
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_byManager_success() {
        User owner = createUser(1L, "owner@test.com", "USER");
        User manager = createUser(3L, "manager@test.com", "MANAGER");
        Task task = createTask(200L, owner, null);

        mockSecurityContext("manager@test.com");

        when(taskRepository.findById(200L)).thenReturn(Optional.of(task));
        when(userRepository.findUserByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        SimpleResponse response = taskService.deleteTask(200L);

        assertEquals("Successfully Deleted!", response.getMessage());
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_byUnauthorizedUser_fail() {
        User owner = createUser(1L, "owner@test.com", "USER");
        User stranger = createUser(4L, "stranger@test.com", "USER");
        Task task = createTask(200L, owner, null);

        mockSecurityContext("stranger@test.com");

        when(taskRepository.findById(200L)).thenReturn(Optional.of(task));
        when(userRepository.findUserByEmail("stranger@test.com")).thenReturn(Optional.of(stranger));

        assertThrows(AccessDeniedException.class, () -> {
            taskService.deleteTask(200L);
        });

        verify(taskRepository, never()).delete((Task) any());
    }

}
