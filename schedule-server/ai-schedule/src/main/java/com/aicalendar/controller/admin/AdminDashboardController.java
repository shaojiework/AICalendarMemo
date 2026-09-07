package com.aicalendar.controller.admin;

import com.aicalendar.annotation.RequireRole;
import com.aicalendar.dto.response.DashboardStatsResponse;
import com.aicalendar.dto.response.DashboardTrendResponse;
import com.aicalendar.dto.response.DashboardTypeDistributionResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台仪表盘控制器（仅ADMIN可访问）
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * 获取首页统计数据（用户/日程/纪念日/AI会话总数）
     */
    @RequireRole({"ADMIN"})
    @GetMapping("/stats")
    public Result<DashboardStatsResponse> getStats() {
        return Result.success(adminDashboardService.getStats());
    }

    /**
     * 获取最近7天新增数据趋势
     */
    @RequireRole({"ADMIN"})
    @GetMapping("/trend")
    public Result<DashboardTrendResponse> getTrend() {
        return Result.success(adminDashboardService.getTrend());
    }

    /**
     * 获取日程类型分布统计
     */
    @RequireRole({"ADMIN"})
    @GetMapping("/schedule-type-distribution")
    public Result<DashboardTypeDistributionResponse> getScheduleTypeDistribution() {
        return Result.success(adminDashboardService.getScheduleTypeDistribution());
    }
}
