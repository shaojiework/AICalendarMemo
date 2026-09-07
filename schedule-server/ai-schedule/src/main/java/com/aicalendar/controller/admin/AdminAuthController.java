package com.aicalendar.controller.admin;

import com.aicalendar.dto.request.AdminLoginRequest;
import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理员认证控制器（登录接口匿名访问，白名单已放行）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminUserService adminUserService;

    public AdminAuthController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 管理员登录：校验账号密码 + 角色，签发JWT
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.success("登录成功", adminUserService.adminLogin(request));
    }
}
