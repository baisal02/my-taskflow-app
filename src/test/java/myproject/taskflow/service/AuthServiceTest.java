package myproject.taskflow.service;

import myproject.taskflow.config.jwt.JwtService;
import myproject.taskflow.dto.request.LoginRequest;
import myproject.taskflow.dto.request.RegisterRequest;
import myproject.taskflow.dto.response.AuthResponse;
import myproject.taskflow.dto.response.LogOutResponse;
import myproject.taskflow.dto.response.RefreshTokenResponse;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.entities.RefreshToken;
import myproject.taskflow.entities.User;
import myproject.taskflow.enums.Role;
import myproject.taskflow.repositories.jpa.RefreshTokenRepository;
import myproject.taskflow.repositories.jpa.UserRepository;
import myproject.taskflow.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest("nick", "email@test.com", "pass", "John", "Doe");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setNickname(req.getNickname());
        savedUser.setEmail(req.getEmail());
        savedUser.setPassword("encodedPass");
        savedUser.setRole(Role.USER);

        when(passwordEncoder.encode(req.getPassword())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refreshToken");

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertEquals("nick", response.getNickname());
        assertEquals(Role.USER, response.getRole());
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest("email@test.com", "pass");
        User user = new User();
        user.setId(1L);
        user.setNickname("nick");
        user.setEmail(req.getEmail());
        user.setPassword("encodedPass");
        user.setRole(Role.USER);

        when(userRepository.findUserByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refreshToken");

        AuthResponse response = authService.login(req);

        assertEquals("nick", response.getNickname());
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_userNotFound() {
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.login(new LoginRequest("no@mail.com", "pass")));
        verify(userRepository).findUserByEmail(anyString());
    }

    @Test
    void login_wrongPassword() {
        User user = new User();
        user.setPassword("encodedPass");
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(new LoginRequest("email@test.com", "wrong")));
        verify(userRepository).findUserByEmail(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    void refreshAccessToken_success() {
        User user = new User();
        user.setEmail("email@test.com");

        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setToken("refreshToken");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        token.setRevoked(false);

        when(refreshTokenRepository.findByToken("refreshToken")).thenReturn(Optional.of(token));
        when(jwtService.generateToken(user)).thenReturn("newAccessToken");

        RefreshTokenResponse resp = authService.refreshAccessToken("refreshToken");

        assertEquals("newAccessToken", resp.getAccessToken());
        verify(jwtService).generateToken(user);
    }

    @Test
    void refreshAccessToken_tokenNotFound() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("invalid"));
        verify(refreshTokenRepository).findByToken(anyString());
    }

    @Test
    void refreshAccessToken_expiredOrRevoked() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        token.setRevoked(false);
        when(refreshTokenRepository.findByToken("refresh")).thenReturn(Optional.of(token));
        assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("refresh"));
        verify(refreshTokenRepository).findByToken("refresh");

        reset(refreshTokenRepository);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        token.setRevoked(true);
        when(refreshTokenRepository.findByToken("refresh")).thenReturn(Optional.of(token));
        assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("refresh"));
        verify(refreshTokenRepository).findByToken("refresh");
    }

    @Test
    void logout_success() {
        RefreshToken token = new RefreshToken();
        token.setToken("refresh");
        token.setRevoked(false);

        when(refreshTokenRepository.findByToken("refresh")).thenReturn(Optional.of(token));

        LogOutResponse resp = authService.logout("refresh");

        assertTrue(token.getRevoked());
        assertEquals(HttpStatus.OK, resp.getStatus());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void logout_tokenNotFound() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.logout("invalid"));
        verify(refreshTokenRepository).findByToken(anyString());
    }

    @Test
    void registerManager_success() {
        RegisterRequest req = new RegisterRequest("manager", "manager@test.com", "pass", "John", "Doe");
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setNickname("manager");

        when(passwordEncoder.encode(req.getPassword())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        SimpleResponse resp = authService.registerManager(req);

        assertTrue(resp.getMessage().contains("manager"));
        assertEquals(HttpStatus.OK, resp.getStatus());
        verify(userRepository).save(any(User.class));
    }
}
