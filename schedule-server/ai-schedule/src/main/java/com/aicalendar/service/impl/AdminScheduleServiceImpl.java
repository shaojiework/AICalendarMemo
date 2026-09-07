package com.aicalendar.service.impl;

import com.aicalendar.common.BizException;
import com.aicalendar.common.ResponseCode;
import com.aicalendar.dto.response.AdminScheduleResponse;
import com.aicalendar.dto.response.PageResponse;
import com.aicalendar.entity.Schedule;
import com.aicalendar.mapper.ScheduleMapper;
import com.aicalendar.service.AdminScheduleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台日程管理服务实现：分页查询、详情、删除
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminScheduleServiceImpl implements AdminScheduleService {

    private final ScheduleMapper scheduleMapper;

    @Override
    public PageResponse<AdminScheduleResponse> pageSchedules(String keyword, String type, String date, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<Schedule> wrapper = Wrappers.<Schedule>lambdaQuery()
                .like(StringUtils.hasText(keyword), Schedule::getTitle, keyword)
                .eq(StringUtils.hasText(type), Schedule::getType, type)
                .orderByDesc(Schedule::getStartTime);

        // 日期过滤：开始时间落在指定当天 [00:00, 次日00:00)
        if (StringUtils.hasText(date)) {
            LocalDate day = LocalDate.parse(date);
            LocalDateTime startOfDay = day.atStartOfDay();
            LocalDateTime nextDayStart = day.plusDays(1).atStartOfDay();
            wrapper.ge(Schedule::getStartTime, startOfDay).lt(Schedule::getStartTime, nextDayStart);
        }

        List<Schedule> pageList = scheduleMapper.selectList(wrapper);
        PageInfo<Schedule> pageInfo = new PageInfo<>(pageList);
        List<AdminScheduleResponse> list = pageList.stream().map(this::toResponse).toList();
        return PageResponse.of(list, pageInfo);
    }

    @Override
    public AdminScheduleResponse getScheduleById(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BizException(ResponseCode.NOT_FOUND);
        }
        return toResponse(schedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BizException(ResponseCode.NOT_FOUND);
        }
        scheduleMapper.deleteById(id);
        log.info("[后台日程管理] 删除日程: id={}, title={}", id, schedule.getTitle());
    }

    /**
     * 实体转后台日程响应
     */
    private AdminScheduleResponse toResponse(Schedule schedule) {
        AdminScheduleResponse response = new AdminScheduleResponse();
        response.setId(schedule.getId());
        response.setTitle(schedule.getTitle());
        response.setDescription(schedule.getDescription());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());
        response.setLocation(schedule.getLocation());
        response.setType(schedule.getType());
        response.setColor(schedule.getColor());
        response.setCreatedAt(schedule.getCreatedAt());
        return response;
    }
}
