package com.aicalendar.service;

import com.aicalendar.dto.request.AdminLoginRequest;
import com.aicalendar.dto.response.AdminUserResponse;
import com.aicalendar.dto.response.LoginResponse;
import com.aicalendar.dto.response.PageResponse;

/**
 * 后台用户管理服务接口
 */
public interface AdminUserService {

    /**
     * 管理员登录：校验账号密码 + 角色（必须为ADMIN），签发JWT
     *
     * @param request 登录参数
     * @return token与用户信息
     */
    LoginResponse adminLogin(AdminLoginRequest request);

    /**
     * 分页查询用户（按账号/昵称模糊、状态过滤）
     *
     * @param keyword  账号/昵称关键字
     * @param status   状态（1启用/0禁用），null不过滤
     * @param pageNum  页码
     * @param pageSize 每页条数
     */
    PageResponse<AdminUserResponse> pageUsers(String keyword, Integer status, int pageNum, int pageSize);

    /**
     * 修改用户启用/禁用状态
     *
     * @param id     用户ID
     * @param status 状态（1启用/0禁用）
     */
    void updateStatus(Long id, Integer status);
}
