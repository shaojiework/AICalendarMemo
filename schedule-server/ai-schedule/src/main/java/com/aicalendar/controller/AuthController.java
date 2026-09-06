package com.aicalendar.controller;

import com.aicalendar.dto.request.LoginRequest;
import com.aicalendar.dto.request.RegisterRequest;
import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录、注册（匿名访问，拦截器白名单放行）
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 登录：校验通过返回JWT与用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success("登录成功", userService.login(request));
    }

    /**
     * 注册：创建默认USER角色账号
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }
}
