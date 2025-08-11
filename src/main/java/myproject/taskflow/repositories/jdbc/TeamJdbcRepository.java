package myproject.taskflow.repositories.jdbc;

import myproject.taskflow.dto.request.TeamRequest;
import myproject.taskflow.dto.response.TeamResponse;
import myproject.taskflow.dto.response.TeamSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Repository
public class TeamJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public TeamJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TeamSummaryResponse> getTeams() {
        String sql = "select id, name from teams";
        return jdbcTemplate.query(sql, (rs, rowNum)
                -> new TeamSummaryResponse(
                rs.getLong("id"),
                rs.getString("name")
        ));
    }

    public TeamResponse getTeamById(Long id) {
        String sql = "select id, name, description, created_by, created_at, updated_at from teams where id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) ->
                new TeamResponse(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getLong("created_by"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
                )
        );
    }



}
