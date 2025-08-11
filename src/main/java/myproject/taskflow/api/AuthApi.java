package myproject.taskflow.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import myproject.taskflow.dto.request.LoginRequest;
import myproject.taskflow.dto.request.RegisterRequest;
import myproject.taskflow.dto.response.AuthResponse;
import myproject.taskflow.dto.response.LogOutResponse;
import myproject.taskflow.dto.response.RefreshTokenResponse;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.service.AuthService;
import myproject.taskflow.service.RefreshTokenService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthApi {
    private final AuthService authService;

    public AuthApi(AuthService authService) {
        this.authService = authService;

    }

    @Operation(summary = "Register new user", description = "Registers a new user with provided details")
    @PostMapping("/register")
    public AuthResponse register(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Register user data", required = true) @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @Operation(summary = "User login", description = "Authenticates user and returns JWT tokens")
    @PostMapping("/login")
    public AuthResponse login(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Login credentials", required = true) @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @Operation(summary = "Register manager(only ADMIN)", description = "Creates a new manager user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/manager")
    public SimpleResponse registerManager(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Register manager data", required = true) @RequestBody RegisterRequest registerRequest) {
        return authService.registerManager(registerRequest);
    }

    @Operation(summary = "Refresh access token", description = "Refreshes JWT access token using refresh token")
    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(@Parameter(description = "Refresh token", required = true) @RequestParam String refreshToken) {
        return authService.refreshAccessToken(refreshToken);
    }

    @Operation(summary = "Logout user", description = "Logs out user by invalidating the refresh token")
    @PostMapping("/logout")
    public LogOutResponse logout(@Parameter(description = "Refresh token", required = true) @RequestParam String refreshToken) {
        return authService.logout(refreshToken);
    }
}
