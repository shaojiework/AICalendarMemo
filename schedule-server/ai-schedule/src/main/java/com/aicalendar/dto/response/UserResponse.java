package com.aicalendar.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应数据（不含密码等敏感字段）
 */
@Data
public class UserResponse {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像路径 */
    private String avatar;

    /** 手机号 */
    private String phone;

    /** 角色（USER/ADMIN） */
    private String role;

    /** 注册时间 */
    private LocalDateTime createdAt;
}
