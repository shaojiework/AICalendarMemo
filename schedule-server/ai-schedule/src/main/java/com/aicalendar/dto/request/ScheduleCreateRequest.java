package com.aicalendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建日程请求DTO
 */
@Data
public class ScheduleCreateRequest {

    /** 日程标题 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 日程描述 */
    private String description;

    /** 开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 结束时间 */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /** 地点 */
    private String location;

    /** 日程类型 */
    private String type;

    /** 颜色标签 */
    private String color;
}