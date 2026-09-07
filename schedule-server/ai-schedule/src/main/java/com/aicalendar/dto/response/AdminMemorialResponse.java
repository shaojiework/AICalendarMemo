package com.aicalendar.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 后台纪念日管理响应对象
 */
@Data
public class AdminMemorialResponse {

    /** 纪念日ID */
    private Long id;

    /** 纪念日名称 */
    private String name;

    /** 纪念日日期 */
    private LocalDate date;

    /** 类型（normal/love/marriage/birthday/other） */
    private String type;

    /** 描述 */
    private String description;

    /** 是否每年提醒（1是/0否） */
    private Integer isYearly;

    /** 颜色 */
    private String color;

    /** 头像/配图路径 */
    private String avatar;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
