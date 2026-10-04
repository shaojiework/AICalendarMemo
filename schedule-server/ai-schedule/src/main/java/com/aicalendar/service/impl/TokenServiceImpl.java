package com.aicalendar.service.impl;

import com.aicalendar.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 令牌状态服务实现（Redis，单机模式）
 *
 * 旋转与撤销都必须以Redis为准：JWT自身过期时间只是兜底上限，
 * 真正的「能不能继续用」由这里的白名单与黑名单决定。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 旋转脚本：旧jti不在白名单即返回0，命中则一次完成「删旧、加新」
     * KEYS: 1=旧白名单键 2=新白名单键
     * ARGV: 1=role 2=TTL秒
     */
    private static final RedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('EXISTS', KEYS[1]) == 0 then\n" +
            "  return 0\n" +
            "end\n" +
            "redis.call('DEL', KEYS[1])\n" +
            "redis.call('SET', KEYS[2], ARGV[1], 'EX', tonumber(ARGV[2]))\n" +
            "return 1", Long.class);

    @Override
    public void issueRefreshToken(Long userId, String jti, String role, long ttlSeconds) {
        redisTemplate.opsForValue().set(whitelistKey(userId, jti), role, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean rotateRefreshToken(Long userId, String oldJti, String newJti, String role, long ttlSeconds) {
        Long result = redisTemplate.execute(ROTATE_SCRIPT,
                Arrays.asList(whitelistKey(userId, oldJti), whitelistKey(userId, newJti)),
                role, String.valueOf(ttlSeconds));
        boolean success = result != null && result == 1L;
        if (!success) {
            log.warn("[令牌] 用户{}刷新令牌已失效（被使用过或已撤销）: {}", userId, oldJti);
        }
        return success;
    }

    @Override
    public void revokeRefreshToken(Long userId, String jti) {
        if (jti == null || jti.isEmpty()) {
            return;
        }
        redisTemplate.delete(whitelistKey(userId, jti));
        log.info("[令牌] 用户{}刷新令牌已撤销", userId);
    }

    @Override
    public void blacklistAccessToken(String jti, long remainSeconds) {
        if (jti == null || jti.isEmpty()) {
            return;
        }
        // 剩余寿命不足1秒说明令牌已自然过期，无需再占缓存
        long ttl = Math.max(remainSeconds, 1L);
        redisTemplate.opsForValue().set(blacklistKey(jti), "1", ttl, TimeUnit.SECONDS);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(jti)));
    }

    private String whitelistKey(Long userId, String jti) {
        return "rt:" + userId + ":" + jti;
    }

    private String blacklistKey(String jti) {
        return "bl:at:" + jti;
    }
}
