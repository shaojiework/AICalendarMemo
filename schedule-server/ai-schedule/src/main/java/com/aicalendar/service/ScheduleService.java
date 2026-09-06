package com.aicalendar.service;

import com.aicalendar.dto.request.ScheduleCreateRequest;
import com.aicalendar.dto.request.ScheduleUpdateRequest;
import com.aicalendar.dto.response.ScheduleResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程服务接口
 */
public interface ScheduleService {

    /**
     * 创建日程
     */
    ScheduleResponse createSchedule(ScheduleCreateRequest request);

    /**
     * 根据ID获取日程
     */
    ScheduleResponse getScheduleById(Long id);

    /**
     * 获取所有日程
     */
    List<ScheduleResponse> getAllSchedules();

    /**
     * 按日期查询日程
     */
    List<ScheduleResponse> getSchedulesByDate(String date);

    /**
     * 按类型查询日程
     */
    List<ScheduleResponse> getSchedulesByType(String type);

    /**
     * 搜索日程
     */
    List<ScheduleResponse> searchSchedules(String keyword);

    /**
     * 更新日程
     */
    ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest request);

    /**
     * 删除日程
     */
    void deleteSchedule(Long id);
}