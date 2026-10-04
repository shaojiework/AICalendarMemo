package com.aicalendar.dto.response;

import lombok.Data;

/**
 * 令牌对响应（刷新接口返回）
 * 只回传令牌与有效期，不外泄jti等内部标识
 */
@Data
public class TokenResponse {

    /** 新访问令牌 */
    private String token;

    /** 新刷新令牌（旋转后的值，旧刷新令牌随即作废） */
    private String refreshToken;

    /** 访问令牌剩余有效期（秒） */
    private Long expiresIn;

    /** 令牌类型标识 */
    private String tokenType;
}
