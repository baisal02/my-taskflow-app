package myproject.taskflow.dto.response;

public class UserSummaryResponse {
    private Long id;
    private String nickname;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserSummaryResponse(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
}
