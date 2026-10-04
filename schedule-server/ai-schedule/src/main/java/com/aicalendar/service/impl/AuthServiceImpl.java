package com.aicalendar.service.impl;

import com.aicalendar.common.BizException;
import com.aicalendar.common.ResponseCode;
import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.TokenPair;
import com.aicalendar.dto.response.TokenResponse;
import com.aicalendar.entity.User;
import com.aicalendar.mapper.UserMapper;
import com.aicalendar.service.AuthService;
import com.aicalendar.service.TokenService;
import com.aicalendar.util.JwtPayload;
import com.aicalendar.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 双令牌认证服务实现（单机模式）
 *
 * 用法约定：accessToken随请求头携带、只用于业务接口鉴权；
 * refreshToken不参与业务请求，唯一用途是调用刷新接口换取新的一对令牌。
 *
 * 刷新令牌一次一用：Redis白名单是唯一裁决依据，旧值旋转成功后立即删除，
 * 再次使用会因白名单查不到而返回40103，前端据此跳登录页。
 * 登出时把当前访问令牌写入黑名单，使其在自然过期前立即失效。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserMapper userMapper;

    @Override
    public LoginResponse issueTokenPair(User user) {
        TokenPair pair = createPair(user.getId(), user.getRole());
        long refreshTtl = jwtUtil.getRefreshExpireSeconds(user.getRole());
        tokenService.issueRefreshToken(user.getId(), pair.getRefreshJti(), user.getRole(), refreshTtl);

        LoginResponse response = new LoginResponse();
        response.setToken(pair.getAccessToken());
        response.setRefreshToken(pair.getRefreshToken());
        response.setExpiresIn(jwtUtil.getAccessExpireSeconds());
        response.setTokenType("Bearer");
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        return response;
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        // 1. 校验刷新令牌：过期与其他非法一律要求重新登录，不做静默续期
        JwtPayload payload = jwtUtil.verifyRefreshToken(refreshToken);
        if (!payload.isValid()) {
            throw new BizException(ResponseCode.REFRESH_INVALID);
        }
        Long userId = payload.getUserId();
        String oldJti = payload.getJti();

        // 2. 复核账号当前状态：禁用则撤销手里这枚令牌并拒绝刷新（判定前置于旋转，避免签发孤儿令牌）
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            tokenService.revokeRefreshToken(userId, oldJti);
            log.warn("[刷新] 用户{}账号已禁用或不存在，已撤销刷新令牌", userId);
            throw new BizException(ResponseCode.REFRESH_INVALID);
        }

        // 3. 原子旋转：Redis白名单查不到旧jti即视为已用过或已撤销，直接要求重新登录
        TokenPair pair = createPair(userId, payload.getRole());
        long refreshTtl = jwtUtil.getRefreshExpireSeconds(payload.getRole());
        if (!tokenService.rotateRefreshToken(userId, oldJti, pair.getRefreshJti(), payload.getRole(), refreshTtl)) {
            throw new BizException(ResponseCode.REFRESH_INVALID);
        }

        TokenResponse response = new TokenResponse();
        response.setToken(pair.getAccessToken());
        response.setRefreshToken(pair.getRefreshToken());
        response.setExpiresIn(jwtUtil.getAccessExpireSeconds());
        response.setTokenType("Bearer");
        log.info("[刷新] 用户{}令牌对已更新", userId);
        return response;
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        // 登出要求幂等：令牌非法不报错，本地与远端状态清掉即可
        JwtPayload accessPayload = jwtUtil.verifyAccessToken(accessToken);
        if (accessPayload.isValid()) {
            long remain = calcRemainSeconds(accessPayload.getExpiresAt());
            tokenService.blacklistAccessToken(accessPayload.getJti(), remain);
            log.info("[登出] 用户{}访问令牌已拉黑，剩余有效秒数: {}", accessPayload.getUserId(), remain);
        }

        JwtPayload refreshPayload = jwtUtil.verifyRefreshToken(refreshToken);
        if (refreshPayload.isValid()) {
            tokenService.revokeRefreshToken(refreshPayload.getUserId(), refreshPayload.getJti());
        }
    }

    /**
     * 签发一对令牌：各自独立jti，便于分别定位撤销
     */
    private TokenPair createPair(Long userId, String role) {
        String accessJti = jwtUtil.newJti();
        String refreshJti = jwtUtil.newJti();
        return new TokenPair(
                jwtUtil.generateAccessToken(userId, role, accessJti),
                jwtUtil.generateRefreshToken(userId, role, refreshJti),
                accessJti,
                refreshJti);
    }

    /**
     * 计算令牌剩余寿命（秒），用于设定黑名单存活时长
     */
    private long calcRemainSeconds(Date expiresAt) {
        if (expiresAt == null) {
            return 0L;
        }
        return Math.max((expiresAt.getTime() - System.currentTimeMillis()) / 1000L, 0L);
    }
}
