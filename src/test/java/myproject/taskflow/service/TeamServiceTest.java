package myproject.taskflow.service;


import myproject.taskflow.dto.request.TeamRequest;
import myproject.taskflow.dto.request.TeamUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.TeamResponse;
import myproject.taskflow.dto.response.TeamSummaryResponse;
import myproject.taskflow.entities.Team;
import myproject.taskflow.entities.TeamMember;
import myproject.taskflow.entities.User;
import myproject.taskflow.repositories.jdbc.TeamJdbcRepository;
import myproject.taskflow.repositories.jpa.TaskRepository;
import myproject.taskflow.repositories.jpa.TeamMemberRepository;
import myproject.taskflow.repositories.jpa.TeamRepository;
import myproject.taskflow.repositories.jpa.UserRepository;
import myproject.taskflow.service.impl.TeamServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamJdbcRepository teamJdbcRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private User createUser(Long id, String email, String nickname) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setNickname(nickname);
        return user;
    }

    private Team createTeam(Long id, User creator) {
        Team team = new Team();
        team.setId(id);
        team.setCreatedBy(creator);
        team.setName("TeamName");
        team.setDescription("Description");
        return team;
    }

    // Мокаем SecurityContext для возвращения email текущего пользователя
    private void mockSecurityContext(String email) {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createTeam_success() {
        User user = createUser(1L, "user@test.com", "nick");
        mockSecurityContext("user@test.com");

        TeamRequest request = new TeamRequest();
        request.setName("MyTeam");
        request.setDescription("Desc");

        when(userRepository.findUserByEmail("user@test.com")).thenReturn(Optional.of(user));

        Team savedTeam = createTeam(10L, user);
        savedTeam.setName(request.getName());
        savedTeam.setDescription(request.getDescription());
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

        TeamResponse response = teamService.createTeam(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("MyTeam", response.getName());
        assertEquals("Desc", response.getDescription());
        assertEquals(user.getId(), response.getCreatedBy());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void getTeams_returnsList() {
        List<TeamSummaryResponse> list = List.of(new TeamSummaryResponse(1L, "team"));
        when(teamJdbcRepository.getTeams()).thenReturn(list);

        List<TeamSummaryResponse> result = teamService.getTeams();

        assertEquals(list, result);
        verify(teamJdbcRepository).getTeams();
    }

    @Test
    void getTeamById_returnsTeam() {
        TeamResponse resp = new TeamResponse(1L, "team1", "desc", 2L, LocalDateTime.now(), LocalDateTime.now());
        when(teamJdbcRepository.getTeamById(1L)).thenReturn(resp);

        TeamResponse result = teamService.getTeamById(1L);

        assertEquals(resp, result);
        verify(teamJdbcRepository).getTeamById(1L);
    }

    @Test
    void updateTeam_success() {
        Team team = createTeam(5L, createUser(2L, "creator@test.com", "creator"));
        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setName("UpdatedName");
        updateRequest.setDescription("UpdatedDesc");

        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));
        when(teamRepository.save(team)).thenReturn(team);

        SimpleResponse resp = teamService.updateTeam(5L, updateRequest);

        assertEquals("Team Updated!", resp.getMessage());
        assertEquals(HttpStatus.OK, resp.getStatus());
        assertEquals("UpdatedName", team.getName());
        assertEquals("UpdatedDesc", team.getDescription());
        verify(teamRepository).save(team);
    }

    @Test
    void updateTeam_notFound() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        assertThrows(RuntimeException.class, () -> teamService.updateTeam(1L, updateRequest));
    }

    @Test
    void deleteTeam_success() {
        Team team = createTeam(3L, createUser(1L, "creator@test.com", "nick"));
        when(teamRepository.findById(3L)).thenReturn(Optional.of(team));

        SimpleResponse resp = teamService.deleteTeam(3L);

        assertEquals("Deleted successfully", resp.getMessage());
        assertEquals(HttpStatus.OK, resp.getStatus());
        verify(teamMemberRepository).deleteByTeamId(3L);
        verify(teamRepository).delete(team);
    }

    @Test
    void deleteTeam_notFound() {
        when(teamRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> teamService.deleteTeam(10L));
    }

    @Test
    void addMemberToTeam_success() {
        Team team = createTeam(7L, createUser(1L, "creator@test.com", "nick"));
        User user = createUser(4L, "member@test.com", "memberNick");
        when(teamRepository.findById(7L)).thenReturn(Optional.of(team));
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));

        SimpleResponse resp = teamService.addMemberToTeam(7L, 4L);

        assertTrue(resp.getMessage().contains("memberNick"));
        assertTrue(resp.getMessage().contains("TeamName"));
        assertEquals(HttpStatus.OK, resp.getStatus());
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    void addMemberToTeam_teamNotFound() {
        when(teamRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> teamService.addMemberToTeam(1L, 1L));
    }

    @Test
    void addMemberToTeam_userNotFound() {
        Team team = createTeam(7L, createUser(1L, "creator@test.com", "nick"));
        when(teamRepository.findById(7L)).thenReturn(Optional.of(team));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> teamService.addMemberToTeam(7L, 99L));
    }

    @Test
    void removeMemberFromTeam_success() {
        Team team = createTeam(8L, createUser(1L, "creator@test.com", "nick"));
        User user = createUser(5L, "user@test.com", "userNick");
        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setUser(user);

        when(teamRepository.findById(8L)).thenReturn(Optional.of(team));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByTeamIdAndUserId(8L, 5L)).thenReturn(Optional.of(teamMember));

        SimpleResponse resp = teamService.removeMemberFromTeam(8L, 5L);

        assertTrue(resp.getMessage().contains("userNick"));
        assertTrue(resp.getMessage().contains("TeamName"));
        assertEquals(HttpStatus.OK, resp.getStatus());
        verify(teamMemberRepository).delete(teamMember);
    }

    @Test
    void removeMemberFromTeam_teamNotFound() {
        when(teamRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> teamService.removeMemberFromTeam(1L, 1L));
    }

    @Test
    void removeMemberFromTeam_userNotFound() {
        Team team = createTeam(8L, createUser(1L, "creator@test.com", "nick"));
        when(teamRepository.findById(8L)).thenReturn(Optional.of(team));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> teamService.removeMemberFromTeam(8L, 99L));
    }

    @Test
    void removeMemberFromTeam_memberNotInTeam() {
        Team team = createTeam(8L, createUser(1L, "creator@test.com", "nick"));
        User user = createUser(5L, "user@test.com", "userNick");

        when(teamRepository.findById(8L)).thenReturn(Optional.of(team));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByTeamIdAndUserId(8L, 5L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> teamService.removeMemberFromTeam(8L, 5L));
    }
}
