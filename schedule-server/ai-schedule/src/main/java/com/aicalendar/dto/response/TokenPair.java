package com.aicalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 令牌对内部载体：签发与刷新时暂存两类令牌及其唯一标识
 * 不直接返回前端，返回前统一转为LoginResponse/TokenResponse
 */
@Data
@AllArgsConstructor
public class TokenPair {

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 访问令牌jti */
    private String accessJti;

    /** 刷新令牌jti */
    private String refreshJti;
}
