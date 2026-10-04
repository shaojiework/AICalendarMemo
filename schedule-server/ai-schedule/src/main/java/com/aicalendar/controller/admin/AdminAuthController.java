package com.aicalendar.controller.admin;

import com.aicalendar.dto.request.AdminLoginRequest;
import com.aicalendar.dto.request.RefreshTokenRequest;
import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.dto.response.TokenResponse;
import com.aicalendar.service.AdminUserService;
import com.aicalendar.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理员认证控制器
 * 登录与刷新匿名访问（白名单已放行），登出需携带有效访问令牌
 */
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminUserService adminUserService;
    private final AuthService authService;

    public AdminAuthController(AdminUserService adminUserService, AuthService authService) {
        this.adminUserService = adminUserService;
        this.authService = authService;
    }

    /**
     * 管理员登录：校验账号密码 + 角色，签发访问令牌与刷新令牌
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.success("登录成功", adminUserService.adminLogin(request));
    }

    /**
     * 刷新令牌：管理端刷新令牌有效期更短，同样一次一用
     */
    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return Result.success("刷新成功", authService.refresh(request.getRefreshToken()));
    }

    /**
     * 退出登录：拉黑当前访问令牌并撤销本次会话刷新令牌
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
