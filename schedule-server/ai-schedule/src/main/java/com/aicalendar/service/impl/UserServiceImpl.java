package com.aicalendar.service.impl;

import com.aicalendar.common.BizException;
import com.aicalendar.common.ResponseCode;
import com.aicalendar.dto.request.LoginRequest;
import com.aicalendar.dto.request.RegisterRequest;
import com.aicalendar.dto.request.UpdateUserRequest;
import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.UserResponse;
import com.aicalendar.entity.User;
import com.aicalendar.mapper.UserMapper;
import com.aicalendar.service.UserService;
import com.aicalendar.util.JwtUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现：登录校验（BCrypt）、注册
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 按用户名查询账号
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, request.getUsername()));
        // 用户不存在或密码错误统一提示，避免暴露账号是否存在
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ResponseCode.PARAM_ERROR.getCode(), "用户名或密码错误");
        }
        // 账号被禁用
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ResponseCode.FORBIDDEN);
        }

        // 签发JWT并组装响应
        LoginResponse response = new LoginResponse();
        response.setToken(jwtUtil.generateToken(user.getId(), user.getRole()));
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        log.info("[登录] 用户登录成功: {}, 角色: {}", user.getUsername(), user.getRole());
        return response;
    }

    @Override
    public void register(RegisterRequest request) {
        // 用户名查重
        Long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BizException(ResponseCode.PARAM_ERROR.getCode(), "用户名已存在");
        }

        // 创建默认USER角色账号，密码BCrypt加密后入库
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        log.info("[注册] 新用户注册: {}", user.getUsername());
    }

    @Override
    public UserResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResponseCode.NOT_FOUND);
        }
        // 组装响应，不返回密码等敏感字段
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    @Override
    public void updateProfile(Long userId, UpdateUserRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResponseCode.NOT_FOUND);
        }
        // 字段可选，传了才更新
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        userMapper.updateById(user);
        log.info("[个人信息] 用户更新个人信息: {}", user.getUsername());
    }
}
