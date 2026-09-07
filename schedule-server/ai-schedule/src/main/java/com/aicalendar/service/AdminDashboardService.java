package com.aicalendar.service;

import com.aicalendar.dto.response.DashboardStatsResponse;
import com.aicalendar.dto.response.DashboardTrendResponse;
import com.aicalendar.dto.response.DashboardTypeDistributionResponse;

/**
 * 后台仪表盘服务接口
 */
public interface AdminDashboardService {

    /**
     * 获取首页统计数据（用户/日程/纪念日/AI会话总数）
     */
    DashboardStatsResponse getStats();

    /**
     * 获取最近7天新增数据趋势（日程/纪念日/AI会话每日新增量）
     */
    DashboardTrendResponse getTrend();

    /**
     * 获取日程类型分布统计
     */
    DashboardTypeDistributionResponse getScheduleTypeDistribution();
}
