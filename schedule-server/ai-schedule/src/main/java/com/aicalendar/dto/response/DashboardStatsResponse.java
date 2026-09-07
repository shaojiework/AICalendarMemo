package com.aicalendar.dto.response;

import lombok.Data;

/**
 * 后台仪表盘统计响应对象
 */
@Data
public class DashboardStatsResponse {

    /** 总用户数 */
    private long userCount;

    /** 日程总数 */
    private long scheduleCount;

    /** 纪念日总数 */
    private long memorialCount;

    /** AI会话总数（按对话ID去重） */
    private long conversationCount;
}
