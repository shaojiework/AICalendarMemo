package com.aicalendar.interceptor;

import com.aicalendar.common.ResponseCode;
import com.aicalendar.common.TokenStatus;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.TokenService;
import com.aicalendar.util.JwtPayload;
import com.aicalendar.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT登录拦截器（双令牌模式）
 * 只接受访问令牌，校验顺序：类型与签名 → 登出黑名单 → 有效期
 * 过期与其他失效分开返回业务码，前端凭40101静默刷新重试、凭40102清态跳登录
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行CORS预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // 无token或格式错误，按非法登录态处理
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, ResponseCode.TOKEN_INVALID);
            return false;
        }

        // 访问令牌校验：以访问密钥验签，刷新令牌在此处会因密钥不匹配被拒绝
        JwtPayload payload = jwtUtil.verifyAccessToken(authHeader.substring(7));
        if (payload.getStatus() == TokenStatus.EXPIRED) {
            // 仅超时：签名合法，前端可凭刷新令牌换取新访问令牌后重试
            writeError(response, ResponseCode.TOKEN_EXPIRED);
            return false;
        }
        if (!payload.isValid()) {
            writeError(response, ResponseCode.TOKEN_INVALID);
            return false;
        }

        // 已登出的令牌在自然过期前仍可通过验签，必须查黑名单拦截
        if (tokenService.isAccessTokenBlacklisted(payload.getJti())) {
            writeError(response, ResponseCode.TOKEN_INVALID);
            return false;
        }

        // userId、role存入request域，供Controller与RoleAspect使用
        request.setAttribute("userId", payload.getUserId());
        request.setAttribute("role", payload.getRole());
        return true;
    }

    /**
     * 输出未授权响应：HTTP状态统一401，具体原因由返回体code区分
     */
    private void writeError(HttpServletResponse response, ResponseCode code) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code)));
    }
}
