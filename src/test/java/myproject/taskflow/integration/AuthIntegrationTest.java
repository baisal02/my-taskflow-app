package myproject.taskflow.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import myproject.taskflow.dto.request.LoginRequest;
import myproject.taskflow.dto.request.RegisterRequest;
import myproject.taskflow.entities.User;
import myproject.taskflow.enums.Role;
import myproject.taskflow.repositories.jpa.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
    @SpringBootTest
    @AutoConfigureMockMvc
    @TestPropertySource(locations = "classpath:application-test.properties")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private ObjectMapper objectMapper;

        private String adminToken;

        @BeforeAll
        void setupAdmin() throws Exception {
            User admin = userRepository.findUserByEmail("admin@example.com")
                    .orElseGet(() -> {
                        User u = new User("AdminNick", "admin@example.com",
                                passwordEncoder.encode("adminpass"), "Admin", "User", Role.ADMIN);
                        return userRepository.save(u);
                    });

            var loginRequest = new LoginRequest("admin@example.com", "adminpass");
            var response = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = response.getResponse().getContentAsString();
            adminToken = JsonPath.read(json, "$.accessToken");
        }

        @Test
        void registerUser_shouldCreateUser() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setNickname("testUser");
            request.setEmail("testuser@example.com");
            request.setPassword("testpass");
            request.setFirstName("Test");
            request.setLastName("User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nickname").value("testUser"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void loginUser_shouldReturnTokens() throws Exception {
            if (userRepository.findUserByEmail("loginuser@example.com").isEmpty()) {
                User u = new User("LoginNick", "loginuser@example.com",
                        passwordEncoder.encode("loginpass"), "Login", "User", Role.USER);
                userRepository.save(u);
            }

            LoginRequest loginRequest = new LoginRequest("loginuser@example.com", "loginpass");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void registerManager_shouldFailWithoutAdminRole() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setNickname("managerUser");
            request.setEmail("manageruser@example.com");
            request.setPassword("managerpass");
            request.setFirstName("Manager");
            request.setLastName("User");

            mockMvc.perform(post("/api/auth/manager")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void registerManager_shouldSucceedWithAdminRole() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setNickname("managerUser");
            request.setEmail("manageruser@example.com");
            request.setPassword("managerpass");
            request.setFirstName("Manager");
            request.setLastName("User");

            mockMvc.perform(post("/api/auth/manager")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("added successfully as manager")));
        }

        @Test
        void refreshAccessToken_shouldReturnNewToken() throws Exception {
            LoginRequest loginRequest = new LoginRequest("admin@example.com", "adminpass");
            var loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String loginJson = loginResult.getResponse().getContentAsString();
            String refreshToken = JsonPath.read(loginJson, "$.refreshToken");

            mockMvc.perform(post("/api/auth/refresh")
                            .param("refreshToken", refreshToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty());
        }

    }
