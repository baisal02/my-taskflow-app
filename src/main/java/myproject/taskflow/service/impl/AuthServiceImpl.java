package myproject.taskflow.service.impl;

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
import myproject.taskflow.service.AuthService;
import myproject.taskflow.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtService jwtService, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        User user = new User();
        user.setNickname(registerRequest.getNickname());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setRole(Role.USER);
        userRepository.save(user);
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(user.getId(),
                user.getNickname(),
                user.getRole(),
                accessToken,
                refreshToken
                );
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
       User user =  userRepository.findUserByEmail(loginRequest.getEmail()).orElseThrow(()->new RuntimeException("User not found"));
       if(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
           return new AuthResponse(user.getId(), user.getNickname(), user.getRole(),jwtService.generateToken(user),refreshTokenService.createRefreshToken(user));
       }
        throw new RuntimeException("Wrong password");
    }

    @Override
    public RefreshTokenResponse refreshAccessToken(String refreshToken) {
        RefreshToken refToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow(()->new RuntimeException("Refresh token not found"));
        if (refToken.getExpiryDate().isBefore(LocalDateTime.now())|| refToken.getRevoked()){
            throw new RuntimeException("Refresh token is not accessible");
        }
        User user = refToken.getUser();
        String accessToken = jwtService.generateToken(user);
        return new RefreshTokenResponse(refToken.getId(), user.getEmail(),accessToken);
    }

    @Override
    public LogOutResponse logout(String refreshToken) {
        RefreshToken refToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow(()->new RuntimeException("Refresh token not found"));
        refToken.setRevoked(true);
        refreshTokenRepository.save(refToken);
        return new LogOutResponse("LogOut succesffully",HttpStatus.OK);
    }

    @Override
    public SimpleResponse registerManager(RegisterRequest registerRequest) {
        User user = new User();
        user.setNickname(registerRequest.getNickname());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setRole(Role.MANAGER);
        userRepository.save(user);

        return new SimpleResponse(
                user.getId()+" "+user.getNickname()+" added successfully as manager",
                HttpStatus.OK);
    }
}
