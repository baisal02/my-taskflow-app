package myproject.taskflow.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import myproject.taskflow.dto.request.TaskRequest;
import myproject.taskflow.dto.request.TaskUpdateRequest;
import myproject.taskflow.entities.Task;
import myproject.taskflow.entities.User;
import myproject.taskflow.enums.Role;
import myproject.taskflow.enums.TaskStatus;
import myproject.taskflow.repositories.jpa.TaskRepository;
import myproject.taskflow.repositories.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User normalUser;
    private User managerUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.findUserByEmail("admin").orElseGet(() -> {
            User u = new User();
            u.setEmail("admin");
            u.setPassword(passwordEncoder.encode("admin"));
            u.setFirstName("Adminbek");
            u.setLastName("Adminbekov");
            u.setRole(Role.ADMIN);
            u.setNickname("adminYo");
            return userRepository.save(u);
        });

        normalUser = userRepository.findUserByEmail("user@example.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("user@example.com");
            u.setPassword(passwordEncoder.encode("userpass"));
            u.setFirstName("User");
            u.setLastName("Test");
            u.setRole(Role.USER);
            u.setNickname("userNick");
            return userRepository.save(u);
        });

        managerUser = userRepository.findUserByEmail("manager@example.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("manager@example.com");
            u.setPassword(passwordEncoder.encode("managerpass"));
            u.setFirstName("Manager");
            u.setLastName("Test");
            u.setRole(Role.MANAGER);
            u.setNickname("managerNick");
            return userRepository.save(u);
        });

        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Description");
        testTask.setStatus(TaskStatus.NEW);
        testTask.setPriority(1);
        testTask.setCategory("General");
        testTask.setDeadline(LocalDateTime.now().plusDays(7));
        testTask.setCreatedBy(adminUser);
        testTask.setAssignedTo(normalUser);
        testTask = taskRepository.save(testTask);
    }

    private String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());  // поддержка LocalDateTime
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createTask_shouldReturnOk_forAuthenticatedUser() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setDescription("Task desc");
        request.setPriority(1);
        request.setCategory("Work");
        request.setDeadline(LocalDateTime.now().plusDays(7));
        request.setAssigned_to(normalUser.getId());

        mockMvc.perform(post("/api/tasks")
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    void getTaskById_shouldReturnTask() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", testTask.getId())
                        .with(user(normalUser.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void updateTask_shouldUpdate_whenUserHasPermission() throws Exception {
        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription("Updated Desc");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(1);
        updateRequest.setCategory("UpdatedCategory");
        updateRequest.setAssignedToId(normalUser.getId());
        updateRequest.setDeadline(LocalDateTime.now().plusDays(7));

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                        .with(user(adminUser.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated successfully!"));
    }

    @Test
    void deleteTask_shouldAllow_forOwnerOrAdmin() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId())
                        .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully Deleted!"));
    }

    @Test
    void deleteTask_shouldDeny_forUnauthorizedUser() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId())
                        .with(user(normalUser.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeTaskStatus_shouldAllow_forOwnerOrAssigneeOrManager() throws Exception {
        mockMvc.perform(patch("/api/tasks/{id}/status", testTask.getId())
                        .param("status", "IN_PROGRESS")
                        .with(user(normalUser.getEmail()).roles("USER"))) // normalUser — assignedTo
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Status Successfully Changed!"));
    }

    @Test
    void changeTaskStatus_shouldDeny_forUnauthorizedUser() throws Exception {
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setPassword(passwordEncoder.encode("pass"));
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setRole(Role.USER);
        otherUser.setNickname("otherNick");
        userRepository.save(otherUser);

        mockMvc.perform(patch("/api/tasks/{id}/status", testTask.getId())
                        .param("status", "COMPLETED")
                        .with(user(otherUser.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignTaskToAnotherPerson_shouldAllow_forOwnerOrManagerOrAdmin() throws Exception {
        User newAssignee = new User();
        newAssignee.setEmail("newassignee@example.com");
        newAssignee.setPassword(passwordEncoder.encode("pass"));
        newAssignee.setFirstName("New");
        newAssignee.setLastName("Assignee");
        newAssignee.setRole(Role.USER);
        newAssignee.setNickname("newAssigneeNick");
        userRepository.save(newAssignee);

        mockMvc.perform(patch("/api/tasks/{userId}/assign", newAssignee.getId())
                        .param("taskId", testTask.getId().toString())
                        .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task Successfully reasigned"));
    }

    @Test
    void assignTaskToAnotherPerson_shouldDeny_forUnauthorizedUser() throws Exception {
        User newAssignee = new User();
        newAssignee.setEmail("newassignee2@example.com");
        newAssignee.setPassword(passwordEncoder.encode("pass"));
        newAssignee.setFirstName("New2");
        newAssignee.setLastName("Assignee2");
        newAssignee.setRole(Role.USER);
        newAssignee.setNickname("newAssigneeNick2");
        userRepository.save(newAssignee);

        mockMvc.perform(patch("/api/tasks/{userId}/assign", newAssignee.getId())
                        .param("taskId", testTask.getId().toString())
                        .with(user(normalUser.getEmail()).roles("USER"))) // обычный пользователь без прав
                .andExpect(status().isForbidden());
    }
}

