package myproject.taskflow.service;

import myproject.taskflow.dto.response.AuthResponse;
import myproject.taskflow.dto.request.LoginRequest;
import myproject.taskflow.dto.response.LogOutResponse;
import myproject.taskflow.dto.response.RefreshTokenResponse;
import myproject.taskflow.dto.request.RegisterRequest;
import myproject.taskflow.dto.response.SimpleResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
    RefreshTokenResponse refreshAccessToken(String refreshToken);
    LogOutResponse logout(String refreshToken);
    SimpleResponse registerManager(RegisterRequest registerRequest);   //
}
