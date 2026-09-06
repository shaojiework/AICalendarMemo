package com.aicalendar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体（前台用户与管理端账号共用，role字段区分角色）
 */
@Data
@TableName("user")
public class User {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（唯一） */
    private String username;

    /** 密码（BCrypt加密存储，禁止明文） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像路径 */
    private String avatar;

    /** 手机号 */
    private String phone;

    /** 角色（USER/ADMIN） */
    private String role;

    /** 状态（1启用/0禁用） */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
