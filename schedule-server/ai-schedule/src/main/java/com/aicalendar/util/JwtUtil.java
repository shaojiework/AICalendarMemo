package com.aicalendar.util;

import com.aicalendar.common.TokenStatus;
import com.aicalendar.common.TokenType;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * JWT工具类：双令牌模式的签发与校验
 * 访问令牌与刷新令牌使用各自独立密钥签名，校验时密钥与令牌类型绑定，
 * 拿刷新令牌冒充访问令牌会因签名验证失败直接拒绝
 * 载荷仅存放userId、role、jti与令牌类型，禁止放入密码等敏感数据
 */
@Component
public class JwtUtil {

    /** 访问令牌签名密钥 */
    @Value("${jwt.access-secret}")
    private String accessSecret;

    /** 刷新令牌签名密钥 */
    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    /** 访问令牌有效期（分钟） */
    @Value("${jwt.access-expire-minutes:30}")
    private int accessExpireMinutes;

    /** 前台刷新令牌有效期（天） */
    @Value("${jwt.refresh-expire-days:7}")
    private int refreshExpireDays;

    /** 后台管理刷新令牌有效期（小时） */
    @Value("${jwt.admin-refresh-expire-hours:12}")
    private int adminRefreshExpireHours;

    /** 生成令牌唯一标识jti */
    public String newJti() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 签发访问令牌（短寿命，随请求头携带）
     *
     * @param userId 用户ID
     * @param role   角色（USER/ADMIN）
     * @param jti    令牌唯一标识
     * @return 签名后的访问令牌
     */
    public String generateAccessToken(Long userId, String role, String jti) {
        Date now = new Date();
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withClaim("typ", TokenType.ACCESS.getValue())
                .withJWTId(jti)
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + accessExpireMinutes * 60_000L))
                .sign(Algorithm.HMAC256(accessSecret));
    }

    /**
     * 签发刷新令牌（长寿命，仅用于换取新令牌对）
     *
     * @param userId 用户ID
     * @param role   角色（USER/ADMIN）
     * @param jti    令牌唯一标识
     * @return 签名后的刷新令牌
     */
    public String generateRefreshToken(Long userId, String role, String jti) {
        Date now = new Date();
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withClaim("typ", TokenType.REFRESH.getValue())
                .withJWTId(jti)
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + getRefreshExpireSeconds(role) * 1000L))
                .sign(Algorithm.HMAC256(refreshSecret));
    }

    /**
     * 校验访问令牌：以访问密钥验签，并复核typ声明
     *
     * @param token 令牌字符串
     * @return 解析结果，状态为VALID/EXPIRED/INVALID
     */
    public JwtPayload verifyAccessToken(String token) {
        return verify(token, accessSecret, TokenType.ACCESS);
    }

    /**
     * 校验刷新令牌：以刷新密钥验签，并复核typ声明
     *
     * @param token 令牌字符串
     * @return 解析结果，状态为VALID/EXPIRED/INVALID
     */
    public JwtPayload verifyRefreshToken(String token) {
        return verify(token, refreshSecret, TokenType.REFRESH);
    }

    /** 访问令牌有效期（秒），登录响应返回给前端计算刷新时机 */
    public long getAccessExpireSeconds() {
        return accessExpireMinutes * 60L;
    }

    /**
     * 刷新令牌有效期（秒）：按角色区分，管理端权限更高、有效期更短
     *
     * @param role 角色（USER/ADMIN）
     */
    public long getRefreshExpireSeconds(String role) {
        if ("ADMIN".equals(role)) {
            return adminRefreshExpireHours * 3600L;
        }
        return refreshExpireDays * 24 * 3600L;
    }

    /**
     * 通用校验：验签→区分过期与非法→复核令牌类型声明
     * 过期令牌理论上仍可验签通过，但本方法不返回其声明内容，过期即需重新签发
     */
    private JwtPayload verify(String token, String secret, TokenType expectType) {
        if (token == null || token.isEmpty()) {
            return JwtPayload.invalid();
        }
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
            // 密钥正确但仍需确认typ与预期一致，防止两类令牌互串使用
            if (!expectType.getValue().equals(jwt.getClaim("typ").asString())) {
                return JwtPayload.invalid();
            }
            return JwtPayload.valid(
                    jwt.getClaim("userId").asLong(),
                    jwt.getClaim("role").asString(),
                    jwt.getId(),
                    expectType,
                    jwt.getExpiresAt());
        } catch (TokenExpiredException e) {
            // 签名合法仅超时：访问令牌可由前端触发静默刷新，刷新令牌则需重新登录
            return JwtPayload.expired();
        } catch (Exception e) {
            // 签名不匹配、格式非法、缺声明统一按非法处理
            return JwtPayload.invalid();
        }
    }
}
