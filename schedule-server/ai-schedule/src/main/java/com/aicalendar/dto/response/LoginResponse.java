package com.aicalendar.dto.response;

import lombok.Data;

/**
 * 登录响应数据（双令牌 + 用户基本信息）
 * token字段即访问令牌，保留原有命名以兼容已上线的前端页面
 */
@Data
public class LoginResponse {

    /** 访问令牌（随请求头Authorization携带） */
    private String token;

    /** 刷新令牌（仅用于调用刷新接口换取新令牌对） */
    private String refreshToken;

    /** 访问令牌剩余有效期（秒），前端据此提前静默续期 */
    private Long expiresIn;

    /** 令牌类型标识，固定为Bearer */
    private String tokenType;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像路径 */
    private String avatar;

    /** 角色（USER/ADMIN） */
    private String role;
}
