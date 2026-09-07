package com.aicalendar.controller.admin;

import com.aicalendar.annotation.RequireRole;
import com.aicalendar.dto.request.UserStatusRequest;
import com.aicalendar.dto.response.AdminUserResponse;
import com.aicalendar.dto.response.PageResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 后台用户管理控制器（仅ADMIN可访问）
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 分页查询用户列表
     */
    @RequireRole({"ADMIN"})
    @GetMapping
    public Result<PageResponse<AdminUserResponse>> pageUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminUserService.pageUsers(keyword, status, page, pageSize));
    }

    /**
     * 修改用户启用/禁用状态
     */
    @RequireRole({"ADMIN"})
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        adminUserService.updateStatus(id, request.getStatus());
        return Result.success("状态更新成功", null);
    }
}
