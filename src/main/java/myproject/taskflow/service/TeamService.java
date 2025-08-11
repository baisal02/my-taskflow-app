package myproject.taskflow.service;

import myproject.taskflow.dto.request.TeamRequest;
import myproject.taskflow.dto.request.TeamUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.TeamResponse;
import myproject.taskflow.dto.response.TeamSummaryResponse;

import java.util.List;

public interface TeamService {
    TeamResponse createTeam(TeamRequest teamRequest);// for admin manager
    List<TeamSummaryResponse> getTeams();
    TeamResponse getTeamById(Long id);
    SimpleResponse updateTeam(Long id,TeamUpdateRequest teamUpdateRequest);
    SimpleResponse deleteTeam(Long id);
    SimpleResponse addMemberToTeam(Long teamId, Long memberId); //manager admin
    SimpleResponse removeMemberFromTeam(Long teamId, Long memberId);   // manager  admin
}
