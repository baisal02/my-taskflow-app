package myproject.taskflow.service.impl;

import jakarta.transaction.Transactional;
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
import myproject.taskflow.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamJdbcRepository teamJdbcRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskRepository taskRepository;

    public TeamServiceImpl(UserRepository userRepository, TeamRepository teamRepository, TeamJdbcRepository teamJdbcRepository, TeamMemberRepository teamMemberRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamJdbcRepository = teamJdbcRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public TeamResponse createTeam(TeamRequest teamRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Team team = new Team();
        team.setCreatedBy(currentUser);
        team.setDescription(teamRequest.getDescription());
        team.setName(teamRequest.getName());

        Team savedTeam = teamRepository.save(team);

        return new TeamResponse(
                savedTeam.getId(),
                savedTeam.getName(),
                savedTeam.getDescription(),
                savedTeam.getCreatedBy().getId(),
                savedTeam.getCreatedAt(),
                savedTeam.getUpdatedAt()
        );
    }


    @Override
    public List<TeamSummaryResponse> getTeams() {
        return teamJdbcRepository.getTeams();
    }

    @Override
    public TeamResponse getTeamById(Long id) {
        return teamJdbcRepository.getTeamById(id);
    }

    @Override
    public SimpleResponse updateTeam(Long id,TeamUpdateRequest teamUpdateRequest) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        team.setName(teamUpdateRequest.getName());
        team.setDescription(teamUpdateRequest.getDescription());
        teamRepository.save(team);
        return new SimpleResponse("Team Updated!", HttpStatus.OK);
    }

    @Transactional
    @Override
    public SimpleResponse deleteTeam(Long id) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        teamMemberRepository.deleteByTeamId(team.getId());
        teamRepository.delete(team);
        return new SimpleResponse("Deleted successfully",HttpStatus.OK);
    }

    @Override
    public SimpleResponse addMemberToTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        User user = userRepository.findById(memberId).orElseThrow(()->new RuntimeException("User not found"));

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setUser(user);
        teamMemberRepository.save(teamMember);
        return new SimpleResponse(user.getNickname()+" Added to the "+team.getName(), HttpStatus.OK);
    }

    @Override
    public SimpleResponse removeMemberFromTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, memberId)
                .orElseThrow(() -> new RuntimeException("User is not in this team"));

        teamMemberRepository.delete(teamMember);

        return new SimpleResponse(user.getNickname() + " removed from " + team.getName(), HttpStatus.OK);
    }

}
