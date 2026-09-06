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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日程服务实现类
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;

    public ScheduleServiceImpl(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreateRequest request) {
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(request, schedule);
        
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
    public ScheduleResponse getScheduleById(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new IllegalArgumentException("日程不存在");
        }
        return convertToResponse(schedule);
    }

    @Override
    public List<ScheduleResponse> getAllSchedules() {
        List<Schedule> schedules = scheduleMapper.selectList(null);
        return schedules.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getSchedulesByDate(String date) {
        LocalDateTime startOfDay = DateUtil.getStartOfDay(date);
        LocalDateTime endOfDay = DateUtil.getEndOfDay(date);
        List<Schedule> schedules = scheduleMapper.selectByDate(startOfDay, endOfDay);
        return schedules.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getSchedulesByType(String type) {
        List<Schedule> schedules = scheduleMapper.selectByType(type);
        return schedules.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> searchSchedules(String keyword) {
        List<Schedule> schedules = scheduleMapper.searchByKeyword(keyword);
        return schedules.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new IllegalArgumentException("日程不存在");
        }

        BeanUtils.copyProperties(request, schedule);
        scheduleMapper.updateById(schedule);
        
        return convertToResponse(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
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