package myproject.taskflow.api;

import io.swagger.v3.oas.annotations.Operation;
import myproject.taskflow.dto.request.TeamRequest;
import myproject.taskflow.dto.request.TeamUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.TeamResponse;
import myproject.taskflow.dto.response.TeamSummaryResponse;
import myproject.taskflow.service.TeamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamApi {
    private final TeamService teamService;

    public TeamApi(TeamService teamService) {
        this.teamService = teamService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Create a new team",
            description = "Allows ADMIN or MANAGER to create a new team with the given details."
    )
    @PostMapping
    public TeamResponse createTeam(@RequestBody TeamRequest team) {
        return teamService.createTeam(team);
    }

    @Operation(
            summary = "Get all teams",
            description = "Retrieves a list of all existing teams with basic information."
    )
    @GetMapping
    public List<TeamSummaryResponse> getAllTeams() {
        return teamService.getTeams();
    }

    @Operation(
            summary = "Get team by ID",
            description = "Fetches detailed information about a specific team by its ID."
    )
    @GetMapping("/{id}")
    public TeamResponse getTeamById(@PathVariable Long id) {
        return teamService.getTeamById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Update an existing team",
            description = "Allows ADMIN or MANAGER to update the details of a team."
    )
    @PutMapping("/{id}")
    public SimpleResponse updateTeam(@PathVariable Long id, @RequestBody TeamUpdateRequest team) {
        return teamService.updateTeam(id, team);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a team",
            description = "Allows ADMIN to delete a team by its ID."
    )

    @DeleteMapping("/{id}")
    public SimpleResponse deleteTeam(@PathVariable Long id) {
        return teamService.deleteTeam(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Add a member to a team",
            description = "Allows ADMIN or MANAGER to add a user to a specific team."
    )

    @PostMapping("/{id}/members")
    public SimpleResponse addMemberToTeam(@PathVariable Long id, @RequestParam Long userId) {
        return teamService.addMemberToTeam(id, userId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Remove a member from a team",
            description = "Allows ADMIN or MANAGER to remove a user from a specific team."
    )

    @DeleteMapping("/{id}/members/{userId}")
    public SimpleResponse removeMemberFromTeam(@PathVariable Long id, @PathVariable Long userId) {
        return teamService.removeMemberFromTeam(id, userId);
    }
}
