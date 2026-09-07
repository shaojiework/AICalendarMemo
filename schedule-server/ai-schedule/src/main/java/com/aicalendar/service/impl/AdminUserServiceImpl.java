package com.aicalendar.service.impl;

import com.aicalendar.common.BizException;
import com.aicalendar.common.ResponseCode;
import com.aicalendar.dto.request.AdminLoginRequest;
import com.aicalendar.dto.response.AdminUserResponse;
import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.PageResponse;
import com.aicalendar.entity.User;
import com.aicalendar.mapper.UserMapper;
import com.aicalendar.service.AdminUserService;
import com.aicalendar.util.JwtUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 后台用户管理服务实现：分页查询、启用/禁用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public LoginResponse adminLogin(AdminLoginRequest request) {
        // 按用户名查询账号
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, request.getUsername()));
        // 用户不存在或密码错误统一提示
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ResponseCode.PARAM_ERROR.getCode(), "用户名或密码错误");
        }
        // 非管理员账号拒绝登录
        if (!"ADMIN".equals(user.getRole())) {
            throw new BizException(ResponseCode.FORBIDDEN.getCode(), "无权限访问后台管理系统");
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
        log.info("[后台登录] 管理员登录成功: {}", user.getUsername());
        return response;
    }

    @Override
    public PageResponse<AdminUserResponse> pageUsers(String keyword, Integer status, int pageNum, int pageSize) {
        // PageHelper 对紧邻的下一条查询分页
        PageHelper.startPage(pageNum, pageSize);
        List<User> pageList = userMapper.selectList(Wrappers.<User>lambdaQuery()
                // 账号或昵称模糊匹配
                .and(StringUtils.hasText(keyword),
                        w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword))
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreatedAt));
        PageInfo<User> pageInfo = new PageInfo<>(pageList);

        // 实体转响应（剔除密码）
        List<AdminUserResponse> list = pageList.stream().map(this::toResponse).toList();
        return PageResponse.of(list, pageInfo);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResponseCode.NOT_FOUND);
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ResponseCode.PARAM_ERROR.getCode(), "状态值非法");
        }
        // 禁止禁用管理员账号，防止后台无法登录
        if (status == 0 && "ADMIN".equals(user.getRole())) {
            throw new BizException(ResponseCode.PARAM_ERROR.getCode(), "不允许禁用管理员账号");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("[后台用户管理] 用户 {} 状态变更为: {}", user.getUsername(), status == 1 ? "启用" : "禁用");
    }

    /**
     * 实体转后台用户响应（不含密码）
     */
    private AdminUserResponse toResponse(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
