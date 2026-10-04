package com.aicalendar.util;

import com.aicalendar.common.TokenStatus;
import com.aicalendar.common.TokenType;
import lombok.Data;

import java.util.Date;

/**
 * JWT解析结果载体
 * 只有status为VALID时字段才有值，调用方禁止在EXPIRED/INVALID下读取用户信息
 */
@Data
public class JwtPayload {

    /** 校验状态 */
    private TokenStatus status;

    /** 用户ID */
    private Long userId;

    /** 角色（USER/ADMIN） */
    private String role;

    /** 令牌唯一标识，登出黑名单与刷新旋转都以它为准 */
    private String jti;

    /** 令牌类型 */
    private TokenType tokenType;

    /** 过期时间，登出时用于计算黑名单需要存活的秒数 */
    private Date expiresAt;

    /** 构建校验通过的解析结果 */
    public static JwtPayload valid(Long userId, String role, String jti, TokenType tokenType, Date expiresAt) {
        JwtPayload payload = new JwtPayload();
        payload.setStatus(TokenStatus.VALID);
        payload.setUserId(userId);
        payload.setRole(role);
        payload.setJti(jti);
        payload.setTokenType(tokenType);
        payload.setExpiresAt(expiresAt);
        return payload;
    }

    /** 构建已过期结果（不含任何声明信息，过期令牌不作为授权依据） */
    public static JwtPayload expired() {
        JwtPayload payload = new JwtPayload();
        payload.setStatus(TokenStatus.EXPIRED);
        return payload;
    }

    /** 构建非法结果 */
    public static JwtPayload invalid() {
        JwtPayload payload = new JwtPayload();
        payload.setStatus(TokenStatus.INVALID);
        return payload;
    }

    /** 是否校验通过 */
    public boolean isValid() {
        return status == TokenStatus.VALID;
    }
}
