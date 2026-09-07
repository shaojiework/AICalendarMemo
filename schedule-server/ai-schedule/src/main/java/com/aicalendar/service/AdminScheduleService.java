package com.aicalendar.service;

import com.aicalendar.dto.response.AdminScheduleResponse;
import com.aicalendar.dto.response.PageResponse;

/**
 * 后台日程管理服务接口
 */
public interface AdminScheduleService {

    /**
     * 分页查询日程（按标题模糊、类型、日期过滤）
     *
     * @param keyword  标题关键字
     * @param type     日程类型
     * @param date     日期（yyyy-MM-dd），按开始时间落在当天过滤
     * @param pageNum  页码
     * @param pageSize 每页条数
     */
    PageResponse<AdminScheduleResponse> pageSchedules(String keyword, String type, String date, int pageNum, int pageSize);

    /**
     * 根据ID查询日程详情
     */
    AdminScheduleResponse getScheduleById(Long id);

    /**
     * 根据ID删除日程
     */
    void deleteSchedule(Long id);
}
