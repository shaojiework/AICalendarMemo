package com.aicalendar.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 仪表盘最近7天趋势响应对象
 */
@Data
public class DashboardTrendResponse {

    /** 日期标签列表（MM-dd格式） */
    private List<String> dates;

    /** 每日新增日程数 */
    private List<Long> scheduleCounts;

    /** 每日新增纪念日数 */
    private List<Long> memorialCounts;

    /** 每日新增AI会话数（按对话ID去重） */
    private List<Long> chatCounts;
}
