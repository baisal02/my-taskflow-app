package myproject.taskflow.dto.response;

public class RefreshTokenResponse {
    private Long userId;
    private String userEmail;
    private String accessToken;

    public RefreshTokenResponse(Long userId, String userEmail, String accessToken) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
