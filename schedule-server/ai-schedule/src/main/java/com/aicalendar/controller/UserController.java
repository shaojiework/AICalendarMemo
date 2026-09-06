package com.aicalendar.controller;

import com.aicalendar.dto.request.UpdateUserRequest;
import com.aicalendar.dto.response.Result;
import com.aicalendar.dto.response.UserResponse;
import com.aicalendar.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息接口：当前登录用户的查询与更新（userId取自JWT解析结果）
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserResponse> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 更新当前登录用户个人信息（昵称/头像/手机号）
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UpdateUserRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.updateProfile(userId, request);
        return Result.success("更新成功", null);
    }
}
