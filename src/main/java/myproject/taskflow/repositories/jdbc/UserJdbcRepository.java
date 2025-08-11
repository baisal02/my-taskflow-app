package myproject.taskflow.repositories.jdbc;

import myproject.taskflow.dto.response.UserDetailResponse;
import myproject.taskflow.dto.response.UserSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserSummaryResponse> getAllUsers() {
        String sql = "select id,nickname from users where role = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new UserSummaryResponse(
                rs.getLong("id"),
                rs.getString("nickname")
        ),"USER");
    }

    public UserDetailResponse getUserById(Long userId) {
        String sql = """
        SELECT id, first_name, last_name, email, created_at
        FROM users
        WHERE id = ?
    """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new UserDetailResponse(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ),
                userId
        );
    }

}
