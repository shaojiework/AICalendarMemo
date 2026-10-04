package com.aicalendar.service;

import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.TokenResponse;
import com.aicalendar.entity.User;

/**
 * 双令牌认证服务：签发、刷新、登出
 * 前台与后台管理端共用同一套逻辑，刷新令牌有效期按角色区分
 */
public interface AuthService {

    /**
     * 账号校验通过后签发令牌对，并把刷新令牌登记进Redis白名单
     *
     * @param user 已通过密码、状态、角色校验的用户
     * @return 含双令牌与用户信息的登录响应
     */
    LoginResponse issueTokenPair(User user);

    /**
     * 用刷新令牌换取新令牌对
     * 刷新令牌一次一用：旋转成功后旧值立即作废，检出重放则撤销该用户全部会话
     *
     * @param refreshToken 前端提交的刷新令牌
     * @return 新令牌对
     */
    TokenResponse refresh(String refreshToken);

    /**
     * 登出：拉黑当前访问令牌（剩余寿命内立即失效）并撤销本次会话的刷新令牌
     *
     * @param accessToken  当前请求携带的访问令牌原文，用于解析jti与剩余寿命
     * @param refreshToken 前端提交的刷新令牌，用于精确定位要撤销的会话；非法或为空则跳过
     */
    void logout(String accessToken, String refreshToken);
}
