package com.aicalendar.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台用户管理响应对象（不含密码等敏感字段）
 */
@Data
public class AdminUserResponse {

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

    /** 状态（1启用/0禁用） */
    private Integer status;

    /** 注册时间 */
    private LocalDateTime createdAt;
}
