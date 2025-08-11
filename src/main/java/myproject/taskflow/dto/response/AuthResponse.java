package myproject.taskflow.dto.response;

import myproject.taskflow.enums.Role;

public class AuthResponse {
    private Long id;
    private String nickname;
    private Role role;
    private String accessToken;
    private String refreshToken;

    public AuthResponse(Long id, String nickname, Role role, String accessToken, String refreshToken) {
        this.id = id;
        this.nickname = nickname;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public Role getRole() {
        return role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
