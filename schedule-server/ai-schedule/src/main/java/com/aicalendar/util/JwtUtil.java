package com.aicalendar.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类：生成token、解析token、校验token有效期
 * 载荷仅存放userId与role，禁止放入密码等敏感数据
 */
@Component
public class JwtUtil {

    /** 签名密钥 */
    @Value("${jwt.secret}")
    private String secret;

    /** 过期时间（小时） */
    @Value("${jwt.expire-hours:2}")
    private int expireHours;

    /**
     * 生成token
     *
     * @param userId 用户ID
     * @param role   角色（USER/ADMIN）
     * @return 签名后的token字符串
     */
    public String generateToken(Long userId, String role) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expireHours * 3600_000L))
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 校验并解析token（含有效期校验）
     *
     * @param token token字符串
     * @return 解析成功返回DecodedJWT，无效或过期返回null
     */
    public DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
        } catch (Exception e) {
            // 签名不匹配、token过期、格式非法统一返回null
            return null;
        }
    }
}
