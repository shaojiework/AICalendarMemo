package com.aicalendar.service;

/**
 * 令牌状态服务：刷新令牌白名单 + 访问令牌登出黑名单
 *
 * JWT无状态、签发后无法撤回，因此撤销能力完全依赖Redis中的记录：
 *   刷新令牌必须在白名单里且一次一用，用过的旧值立即删除
 *   访问令牌登出后靠黑名单在剩余寿命内拒绝放行
 *
 * 键设计：
 *   rt:{userId}:{jti}    STRING  role    TTL=刷新令牌有效期 —— 白名单，旋转与撤销都以它为准
 *   bl:at:{jti}          STRING  "1"     TTL=访问令牌剩余寿命 —— 登出黑名单
 */
public interface TokenService {

    /**
     * 登记刷新令牌进白名单
     *
     * @param userId     用户ID
     * @param jti        刷新令牌唯一标识
     * @param role       角色，存值便于排查
     * @param ttlSeconds 有效期（秒）
     */
    void issueRefreshToken(Long userId, String jti, String role, long ttlSeconds);

    /**
     * 刷新令牌旋转：原子完成「确认旧值仍在 + 作废旧值 + 登记新值」
     *
     * @return true 旋转成功；false 旧值不存在（已被使用过或已撤销），前端需重新登录
     */
    boolean rotateRefreshToken(Long userId, String oldJti, String newJti, String role, long ttlSeconds);

    /**
     * 撤销本次会话的刷新令牌（退出登录、账号被禁用）
     */
    void revokeRefreshToken(Long userId, String jti);

    /**
     * 拉黑访问令牌，使其在自然过期前立即失效
     *
     * @param jti           访问令牌唯一标识
     * @param remainSeconds 剩余寿命（秒）
     */
    void blacklistAccessToken(String jti, long remainSeconds);

    /**
     * 判断访问令牌是否已被登出拉黑
     */
    boolean isAccessTokenBlacklisted(String jti);
}
