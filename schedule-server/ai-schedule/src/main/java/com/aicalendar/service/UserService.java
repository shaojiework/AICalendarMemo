package com.aicalendar.service;

import com.aicalendar.dto.request.LoginRequest;
import com.aicalendar.dto.request.RegisterRequest;
import com.aicalendar.dto.request.UpdateUserRequest;
import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.UserResponse;

/**
 * 用户服务接口：登录注册、个人信息
 */
public interface UserService {

    /**
     * 登录：校验用户名密码，签发JWT
     *
     * @param request 登录参数
     * @return token与用户信息
     */
    LoginResponse login(LoginRequest request);

    /**
     * 注册：用户名查重后创建默认USER角色账号
     *
     * @param request 注册参数
     */
    void register(RegisterRequest request);

    /**
     * 查询当前登录用户信息（按userId）
     *
     * @param userId 用户ID
     * @return 用户信息（不含密码）
     */
    UserResponse getUserInfo(Long userId);

    /**
     * 更新当前登录用户个人信息（昵称/头像/手机号）
     *
     * @param userId  用户ID
     * @param request 更新参数
     */
    void updateProfile(Long userId, UpdateUserRequest request);
}
