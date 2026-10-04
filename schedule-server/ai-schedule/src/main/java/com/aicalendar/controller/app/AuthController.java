package com.aicalendar.controller.app;

import com.aicalendar.dto.request.LoginRequest;
import com.aicalendar.dto.request.RefreshTokenRequest;
import com.aicalendar.dto.request.RegisterRequest;
import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.dto.response.TokenResponse;
import com.aicalendar.service.AuthService;
import com.aicalendar.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录、注册、令牌刷新、登出
 * 登录注册刷新为匿名访问（白名单放行），登出需携带有效访问令牌
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    /**
     * 登录：校验通过返回访问令牌、刷新令牌与用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success("登录成功", userService.login(request));
    }

    /**
     * 注册：用户名查重后创建默认USER角色账号
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 刷新令牌：凭刷新令牌换取新令牌对，旧刷新令牌一次一用立即作废
     */
    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return Result.success("刷新成功", authService.refresh(request.getRefreshToken()));
    }

    /**
     * 登出：拉黑当前访问令牌并撤销本次会话的刷新令牌
     * 路径不在白名单内，由JwtInterceptor保证令牌合法后进入
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest,
                              @RequestBody(required = false) RefreshTokenRequest request) {
        String authHeader = httpRequest.getHeader("Authorization");
        String accessToken = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : null;
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(accessToken, refreshToken);
        return Result.success("已退出登录", null);
    }
}
