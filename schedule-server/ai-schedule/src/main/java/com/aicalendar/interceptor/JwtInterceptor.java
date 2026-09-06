package com.aicalendar.interceptor;

import com.aicalendar.common.ResponseCode;
import com.aicalendar.dto.response.Result;
import com.aicalendar.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
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
 * JWT登录拦截器：校验token合法性，判断用户是否已登录
 * 解析出的userId、role存入request域，供Controller与RoleAspect使用
 * 白名单（/api/auth/login、/api/auth/register）在WebConfig中放行
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行CORS预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // 无token或格式错误，返回401
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return false;
        }

        // 校验token（签名+有效期），无效返回401
        DecodedJWT jwt = jwtUtil.verifyToken(authHeader.substring(7));
        if (jwt == null) {
            writeUnauthorized(response);
            return false;
        }

        // userId、role存入request域
        request.setAttribute("userId", jwt.getClaim("userId").asLong());
        request.setAttribute("role", jwt.getClaim("role").asString());
        return true;
    }

    /**
     * 返回401未授权响应
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(ResponseCode.UNAUTHORIZED)));
    }
}
