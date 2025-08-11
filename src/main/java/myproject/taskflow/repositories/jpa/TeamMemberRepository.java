package myproject.taskflow.repositories.jpa;

import myproject.taskflow.entities.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    void deleteByUserId(Long userId);

    void deleteByTeamId(Long id);

    Optional<TeamMember> findByUserId(Long id);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long memberId);
}
