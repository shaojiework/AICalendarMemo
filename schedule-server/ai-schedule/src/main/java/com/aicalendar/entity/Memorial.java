package com.aicalendar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 纪念日实体类
 */
@Data
@TableName("memorial")
public class Memorial {

    /** 纪念日ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 纪念日名称 */
    @TableField("name")
    private String name;

    /** 纪念日日期 */
    @TableField("date")
    private LocalDate date;

    /** 类型（love/marriage/birthday/other） */
    @TableField("type")
    private String type;

    /** 描述 */
    @TableField("description")
    private String description;

    /** 是否每年提醒（1是/0否） */
    @TableField("is_yearly")
    private Integer isYearly;

    /** 颜色 */
    @TableField("color")
    private String color;

    /** 头像路径 */
    @TableField("avatar")
    private String avatar;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}