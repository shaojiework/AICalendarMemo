package com.aicalendar.service.impl;

import com.aicalendar.dto.request.ScheduleCreateRequest;
import com.aicalendar.dto.request.ScheduleUpdateRequest;
import com.aicalendar.dto.response.ScheduleResponse;
import com.aicalendar.entity.Schedule;
import com.aicalendar.mapper.ScheduleMapper;
import com.aicalendar.service.ScheduleService;
import com.aicalendar.util.DateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日程服务实现类（按当前登录用户隔离数据，越权访问统一抛"日程不存在"避免暴露存在性）
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;

    public ScheduleServiceImpl(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    @Transactional
    public ScheduleResponse createSchedule(Long userId, ScheduleCreateRequest request) {
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(request, schedule);
        // 绑定当前登录用户
        schedule.setUserId(userId);

        if (schedule.getColor() == null) {
            schedule.setColor("#3B82F6");
        }
        if (schedule.getType() == null) {
            schedule.setType("normal");
        }

        scheduleMapper.insert(schedule);
        return convertToResponse(schedule);
    }

    @Override
    public ScheduleResponse getScheduleById(Long userId, Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        // 越权访问：不暴露他人数据存在性
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            throw new IllegalArgumentException("日程不存在");
        }
        return convertToResponse(schedule);
    }

    @Override
    public List<ScheduleResponse> getAllSchedules(Long userId) {
        // 通过 BaseMapper 的 LambdaQueryWrapper 按用户查询
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Schedule> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getUserId, userId).orderByAsc(Schedule::getStartTime);
        List<Schedule> schedules = scheduleMapper.selectList(wrapper);
        return schedules.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getSchedulesByDate(Long userId, String date) {
        LocalDateTime startOfDay = DateUtil.getStartOfDay(date);
        LocalDateTime endOfDay = DateUtil.getEndOfDay(date);
        List<Schedule> schedules = scheduleMapper.selectByDate(userId, startOfDay, endOfDay);
        return schedules.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getSchedulesByType(Long userId, String type) {
        List<Schedule> schedules = scheduleMapper.selectByType(userId, type);
        return schedules.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> searchSchedules(Long userId, String keyword) {
        List<Schedule> schedules = scheduleMapper.searchByKeyword(userId, keyword);
        return schedules.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Long userId, Long id, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            throw new IllegalArgumentException("日程不存在");
        }

        BeanUtils.copyProperties(request, schedule);
        scheduleMapper.updateById(schedule);

        return convertToResponse(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long userId, Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            throw new IllegalArgumentException("日程不存在");
        }
        scheduleMapper.deleteById(id);
    }

    /**
     * 转换为响应DTO
     */
    private ScheduleResponse convertToResponse(Schedule schedule) {
        ScheduleResponse response = new ScheduleResponse();
        BeanUtils.copyProperties(schedule, response);
        return response;
    }
}
