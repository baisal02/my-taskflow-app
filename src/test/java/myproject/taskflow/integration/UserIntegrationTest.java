package myproject.taskflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.taskflow.dto.request.UserUpdateRequest;
import myproject.taskflow.entities.User;
import myproject.taskflow.enums.Role;
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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.findUserByEmail("admin").orElseGet(() -> {
            User user = new User();
            user.setEmail("admin");
            user.setPassword(passwordEncoder.encode("admin"));
            user.setFirstName("Adminbek");
            user.setLastName("Adminbekov");
            user.setRole(Role.ADMIN);
            user.setNickname("adminYo");
            return userRepository.save(user);
        });

        normalUser = userRepository.findUserByEmail("user@example.com").orElseGet(() -> {
            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("userpass"));
            user.setFirstName("User");
            user.setLastName("Test");
            user.setRole(Role.USER);
            user.setNickname("userNick");
            return userRepository.save(user);
        });

        System.out.println("Admin user id: " + adminUser.getId());
        System.out.println("Normal user id: " + normalUser.getId());
    }


    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUsers_shouldReturnForbidden_forNormalUser() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(user(normalUser.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_shouldReturnOk_forAdmin() throws Exception {
        System.out.println("User count: " + userRepository.count());
        mockMvc.perform(get("/api/users")
                        .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

    }


    @Test
    void getUserById_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/users/{id}", normalUser.getId())
                        .with(user(normalUser.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(normalUser.getEmail()));
    }

    @Test
    void updateOwnProfile_shouldUpdateUser() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("newNickname");
        updateRequest.setFirstName("NewFirst");
        updateRequest.setLastName("NewLast");

        mockMvc.perform(put("/api/users")
                        .with(user(normalUser.getEmail()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated!"));

        User updatedUser = userRepository.findById(normalUser.getId()).orElseThrow();
        assertEquals("newNickname", updatedUser.getNickname());
        assertEquals("NewFirst", updatedUser.getFirstName());
        assertEquals("NewLast", updatedUser.getLastName());
    }

    @Test
    void deleteUser_shouldReturnForbidden_forNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", normalUser.getId())
                        .with(user(normalUser.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_shouldDeleteUser_forAdmin() throws Exception {
        User toDelete = new User();
        toDelete.setEmail("todelete@example.com");
        toDelete.setPassword(passwordEncoder.encode("pass"));
        toDelete.setFirstName("To");
        toDelete.setLastName("Delete");
        toDelete.setRole(Role.USER);
        toDelete.setNickname("toDeleteNick");
        userRepository.save(toDelete);

        mockMvc.perform(delete("/api/users/{id}", toDelete.getId())
                        .with(user(adminUser.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted!"));

        assertTrue(userRepository.findById(toDelete.getId()).isEmpty());
    }
}


