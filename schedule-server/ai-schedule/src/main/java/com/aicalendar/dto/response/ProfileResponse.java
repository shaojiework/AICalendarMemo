package com.aicalendar.dto.response;

/**
 * 用户信息响应DTO
 */
public class ProfileResponse {

    private Long id;

    private String username;

    private String avatar;

    private String description;

    private Integer daysUsing;

    private Integer scheduleCount;

    private Integer memorialCount;

    public ProfileResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getDaysUsing() {
        return daysUsing;
    }

    public void setDaysUsing(Integer daysUsing) {
        this.daysUsing = daysUsing;
    }

    public Integer getScheduleCount() {
        return scheduleCount;
    }

    public void setScheduleCount(Integer scheduleCount) {
        this.scheduleCount = scheduleCount;
    }

    public Integer getMemorialCount() {
        return memorialCount;
    }

    public void setMemorialCount(Integer memorialCount) {
        this.memorialCount = memorialCount;
    }
}