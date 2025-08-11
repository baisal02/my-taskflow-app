package myproject.taskflow.repositories.jpa;

import myproject.taskflow.entities.RefreshToken;
import myproject.taskflow.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String refToken);

    Optional<RefreshToken> findByUserId(Long id);

    void deleteByUser(User user);

    void deleteByUserId(Long userId);
}
