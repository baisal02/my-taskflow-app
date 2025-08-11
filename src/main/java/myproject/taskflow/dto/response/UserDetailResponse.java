package myproject.taskflow.dto.response;

import java.time.LocalDateTime;

public class UserDetailResponse {
    private Long id;
    private String name;
    private String surName;
    private String email;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserDetailResponse() {
    }

    public UserDetailResponse(Long id, String name, String surName, String email, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.surName = surName;
        this.email = email;
        this.createdAt = createdAt;
    }
}
