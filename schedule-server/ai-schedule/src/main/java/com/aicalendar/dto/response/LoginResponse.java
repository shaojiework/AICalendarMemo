package com.aicalendar.dto.response;

import lombok.Data;

/**
 * 登录响应数据（含token与用户基本信息）
 */
@Data
public class LoginResponse {

    /** JWT令牌 */
    private String token;

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
