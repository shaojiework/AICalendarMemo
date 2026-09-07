package com.aicalendar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日程实体类
 */
@Data
@TableName("schedule")
public class Schedule {

    /** 日程ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    @TableField("user_id")
    private Long userId;

    /** 日程标题 */
    @TableField("title")
    private String title;

    /** 日程描述 */
    @TableField("description")
    private String description;

    /** 开始时间 */
    @TableField("start_time")
    private LocalDateTime startTime;

    /** 结束时间 */
    @TableField("end_time")
    private LocalDateTime endTime;

    /** 地点 */
    @TableField("location")
    private String location;

    /** 日程类型（normal/work/personal等） */
    @TableField("type")
    private String type;

    /** 颜色标签 */
    @TableField("color")
    private String color;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}