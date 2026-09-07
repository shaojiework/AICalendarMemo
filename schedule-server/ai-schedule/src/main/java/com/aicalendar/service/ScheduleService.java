package com.aicalendar.service;

import com.aicalendar.dto.request.ScheduleCreateRequest;
import com.aicalendar.dto.request.ScheduleUpdateRequest;
import com.aicalendar.dto.response.ScheduleResponse;

import java.util.List;

/**
 * 日程服务接口（按当前登录用户隔离数据）
 */
public interface ScheduleService {

    /** 创建日程 */
    ScheduleResponse createSchedule(Long userId, ScheduleCreateRequest request);

    /** 根据ID获取日程（校验归属） */
    ScheduleResponse getScheduleById(Long userId, Long id);

    /** 获取某用户所有日程 */
    List<ScheduleResponse> getAllSchedules(Long userId);

    /** 按日期查询某用户日程 */
    List<ScheduleResponse> getSchedulesByDate(Long userId, String date);

    /** 按类型查询某用户日程 */
    List<ScheduleResponse> getSchedulesByType(Long userId, String type);

    /** 搜索某用户日程 */
    List<ScheduleResponse> searchSchedules(Long userId, String keyword);

    /** 更新日程（校验归属） */
    ScheduleResponse updateSchedule(Long userId, Long id, ScheduleUpdateRequest request);

    /** 删除日程（校验归属） */
    void deleteSchedule(Long userId, Long id);
}
