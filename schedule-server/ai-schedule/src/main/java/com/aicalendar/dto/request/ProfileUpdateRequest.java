package com.aicalendar.dto.request;

/**
 * 用户信息更新请求DTO
 */
public class ProfileUpdateRequest {

    private String username;

    private String avatar;

    private String description;

    public ProfileUpdateRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}