package myproject.taskflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.taskflow.dto.request.TeamRequest;
import myproject.taskflow.dto.request.TeamUpdateRequest;
import myproject.taskflow.entities.Team;
import myproject.taskflow.entities.TeamMember;
import myproject.taskflow.entities.User;
import myproject.taskflow.enums.Role;
import myproject.taskflow.repositories.jpa.TeamMemberRepository;
import myproject.taskflow.repositories.jpa.TeamRepository;
import myproject.taskflow.repositories.jpa.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TeamIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User managerUser;
    private User normalUser;

    @BeforeEach
    void cleanUp() {
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
    }


    @BeforeAll
    void setupUsers() {
        adminUser = userRepository.findUserByEmail("admin@example.com")
                .orElseGet(() -> userRepository.save(
                        new User("AdminNick", "admin@example.com", "admin", "Admin", "User", Role.ADMIN)
                ));

        managerUser = userRepository.findUserByEmail("manager@example.com")
                .orElseGet(() -> userRepository.save(
                        new User("ManagerNick", "manager@example.com", "manager", "Manager", "User", Role.MANAGER)
                ));

        normalUser = userRepository.findUserByEmail("user@example.com")
                .orElseGet(() -> userRepository.save(
                        new User("UserNick", "user@example.com", "user", "Normal", "User", Role.USER)
                ));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void createTeam_shouldSucceed_forAdmin() throws Exception {
        TeamRequest request = new TeamRequest();
        request.setName("Test Team");
        request.setDescription("Description");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Team"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void createTeam_shouldFail_forNormalUser() throws Exception {
        TeamRequest request = new TeamRequest();
        request.setName("Test Team");
        request.setDescription("Description");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void getAllTeams_shouldReturnList() throws Exception {
        Team team = new Team();
        team.setName("Existing Team");
        team.setDescription("Desc");
        team.setCreatedBy(adminUser);
        teamRepository.save(team);

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Existing Team"));
    }

    @Test
    @WithMockUser(username = "manager@example.com", roles = {"MANAGER"})
    void updateTeam_shouldSucceed_forManager() throws Exception {
        Team team = new Team();
        team.setName("Old Name");
        team.setDescription("Old Desc");
        team.setCreatedBy(adminUser);
        team = teamRepository.save(team);

        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setName("New Name");
        updateRequest.setDescription("New Desc");

        mockMvc.perform(put("/api/teams/{id}", team.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Team Updated!"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteTeam_shouldSucceed_forAdmin() throws Exception {
        Team team = new Team();
        team.setName("To Delete");
        team.setDescription("Desc");
        team.setCreatedBy(adminUser);
        team = teamRepository.save(team);

        mockMvc.perform(delete("/api/teams/{id}", team.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted successfully"));
    }

    @Test
    @WithMockUser(username = "manager@example.com", roles = {"MANAGER"})
    void addMemberToTeam_shouldSucceed_forManager() throws Exception {
        Team team = new Team();
        team.setName("Team With Member");
        team.setDescription("Desc");
        team.setCreatedBy(adminUser);
        team = teamRepository.save(team);

        User newMember = userRepository.save(
                new User("NewNick", "newmember@example.com", "pass", "New", "Member", Role.USER)
        );

        mockMvc.perform(post("/api/teams/{id}/members", team.getId())
                        .param("userId", newMember.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(newMember.getNickname() + " Added to the " + team.getName()));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void removeMemberFromTeam_shouldSucceed_forAdmin() throws Exception {
        Team team = new Team();
        team.setName("Team Remove Member");
        team.setDescription("Desc");
        team.setCreatedBy(adminUser);
        team = teamRepository.save(team);

        User member = userRepository.save(
                new User("MemberNick", "member@example.com", "pass", "Member", "User", Role.USER)
        );

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setUser(member);
        teamMemberRepository.save(teamMember);

        mockMvc.perform(delete("/api/teams/{id}/members/{userId}", team.getId(), member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(member.getNickname() + " removed from " + team.getName()));
    }
}


