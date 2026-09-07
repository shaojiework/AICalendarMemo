package com.aicalendar.controller.admin;

import com.aicalendar.annotation.RequireRole;
import com.aicalendar.dto.response.AdminScheduleResponse;
import com.aicalendar.dto.response.PageResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.AdminScheduleService;
import org.springframework.web.bind.annotation.*;

/**
 * 后台日程管理控制器（仅ADMIN可访问）
 */
@RestController
@RequestMapping("/api/admin/schedule")
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    public AdminScheduleController(AdminScheduleService adminScheduleService) {
        this.adminScheduleService = adminScheduleService;
    }

    /**
     * 分页查询日程列表（标题关键字/类型/日期过滤）
     */
    @RequireRole({"ADMIN"})
    @GetMapping
    public Result<PageResponse<AdminScheduleResponse>> pageSchedules(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminScheduleService.pageSchedules(keyword, type, date, page, pageSize));
    }

    /**
     * 查询日程详情
     */
    @RequireRole({"ADMIN"})
    @GetMapping("/{id}")
    public Result<AdminScheduleResponse> getScheduleById(@PathVariable Long id) {
        return Result.success(adminScheduleService.getScheduleById(id));
    }

    /**
     * 删除日程
     */
    @RequireRole({"ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        adminScheduleService.deleteSchedule(id);
        return Result.success("删除成功", null);
    }
}
