package myproject.taskflow.service.impl;

import myproject.taskflow.entities.RefreshToken;
import myproject.taskflow.entities.User;
import myproject.taskflow.repositories.jpa.RefreshTokenRepository;
import myproject.taskflow.service.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    @Override
    public String createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(token -> {
                    token.setExpiryDate(LocalDateTime.now().plusDays(7));
                    token.setToken(UUID.randomUUID().toString());
                    return token;
                })
                .orElseGet(() -> {
                    RefreshToken token = new RefreshToken();
                    token.setUser(user);
                    token.setExpiryDate(LocalDateTime.now().plusDays(7));
                    token.setToken(UUID.randomUUID().toString());
                    return refreshTokenRepository.save(token);
                });
        return refreshToken.getToken();
    }





}
