package com.aicalendar.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日程响应DTO
 */
@Data
public class ScheduleResponse {

    /** 日程ID */
    private Long id;

    /** 日程标题 */
    private String title;

    /** 日程描述 */
    private String description;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 地点 */
    private String location;

    /** 日程类型 */
    private String type;

    /** 颜色标签 */
    private String color;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}