package com.aicalendar.service.impl;

import com.aicalendar.dto.response.DashboardStatsResponse;
import com.aicalendar.dto.response.DashboardTrendResponse;
import com.aicalendar.dto.response.DashboardTypeDistributionResponse;
import com.aicalendar.entity.AiChat;
import com.aicalendar.entity.Memorial;
import com.aicalendar.entity.Schedule;
import com.aicalendar.mapper.AiChatMapper;
import com.aicalendar.mapper.MemorialMapper;
import com.aicalendar.mapper.ScheduleMapper;
import com.aicalendar.mapper.UserMapper;
import com.aicalendar.service.AdminDashboardService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台仪表盘服务实现：统计各业务表总量、趋势、分布
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserMapper userMapper;
    private final ScheduleMapper scheduleMapper;
    private final MemorialMapper memorialMapper;
    private final AiChatMapper aiChatMapper;

    /** 日程类型中文映射（value → 中文名） */
    private static final Map<String, String> SCHEDULE_TYPE_MAP = new LinkedHashMap<>() {{
        put("work", "工作");
        put("personal", "个人");
        put("meeting", "会议");
        put("normal", "其他");
    }};

    @Override
    public DashboardStatsResponse getStats() {
        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setUserCount(userMapper.selectCount(null));
        response.setScheduleCount(scheduleMapper.selectCount(null));
        response.setMemorialCount(memorialMapper.selectCount(null));
        // AI会话按对话ID去重统计
        response.setConversationCount(aiChatMapper.countDistinctConversations());
        return response;
    }

    @Override
    public DashboardTrendResponse getTrend() {
        // 计算最近7天日期范围（含今天）
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.minusDays(6);

        LocalDateTime rangeStart = startDay.atStartOfDay();
        LocalDateTime rangeEnd = today.plusDays(1).atStartOfDay();

        // 查询各表在7天范围内的记录，按日期分组统计
        DashboardTrendResponse response = new DashboardTrendResponse();
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dates.add(startDay.plusDays(i).toString().substring(5)); // MM-dd
        }
        response.setDates(dates);

        // 日程按 created_at 统计每日新增
        List<Schedule> schedules = scheduleMapper.selectList(Wrappers.<Schedule>lambdaQuery()
                .ge(Schedule::getCreatedAt, rangeStart).lt(Schedule::getCreatedAt, rangeEnd));
        response.setScheduleCounts(groupByDate(schedules, Schedule::getCreatedAt, startDay));

        // 纪念日按 created_at 统计每日新增
        List<Memorial> memorials = memorialMapper.selectList(Wrappers.<Memorial>lambdaQuery()
                .ge(Memorial::getCreatedAt, rangeStart).lt(Memorial::getCreatedAt, rangeEnd));
        response.setMemorialCounts(groupByDate(memorials, Memorial::getCreatedAt, startDay));

        // AI会话按 created_at 统计每日新增消息数作为活跃度趋势
        List<AiChat> chats = aiChatMapper.selectByTimeRange(rangeStart, rangeEnd);
        response.setChatCounts(groupByDate(chats, AiChat::getCreatedAt, startDay));

        return response;
    }

    @Override
    public DashboardTypeDistributionResponse getScheduleTypeDistribution() {
        // 查询全部日程，按 type 分组计数
        List<Schedule> allSchedules = scheduleMapper.selectList(null);
        Map<String, Long> typeCounts = new LinkedHashMap<>();
        for (Schedule schedule : allSchedules) {
            String type = schedule.getType() != null ? schedule.getType() : "normal";
            typeCounts.merge(type, 1L, Long::sum);
        }

        // 组装响应：按预定义类型顺序，未匹配的类型归为"其他"
        List<DashboardTypeDistributionResponse.TypeItem> items = new ArrayList<>();
        for (Map.Entry<String, String> entry : SCHEDULE_TYPE_MAP.entrySet()) {
            long count = typeCounts.getOrDefault(entry.getKey(), 0L);
            if (count > 0) {
                items.add(new DashboardTypeDistributionResponse.TypeItem(entry.getValue(), count));
            }
        }
        // 未在预定义中的类型归为"其他"
        long otherCount = typeCounts.entrySet().stream()
                .filter(e -> !SCHEDULE_TYPE_MAP.containsKey(e.getKey()))
                .mapToLong(Map.Entry::getValue).sum();
        if (otherCount > 0) {
            items.add(new DashboardTypeDistributionResponse.TypeItem("其他", otherCount));
        }
        return new DashboardTypeDistributionResponse(items);
    }

    /**
     * 通用：将实体列表按 createdAt 字段分组到7天数组中
     */
    private <T> List<Long> groupByDate(List<T> list, java.util.function.Function<T, LocalDateTime> dateExtractor, LocalDate startDay) {
        long[] counts = new long[7];
        for (T item : list) {
            LocalDateTime createdAt = dateExtractor.apply(item);
            if (createdAt != null) {
                int dayOffset = (int) java.time.Duration.between(startDay.atStartOfDay(), createdAt.toLocalDate().atStartOfDay()).toDays();
                if (dayOffset >= 0 && dayOffset < 7) {
                    counts[dayOffset]++;
                }
            }
        }
        List<Long> result = new ArrayList<>(7);
        for (long c : counts) {
            result.add(c);
        }
        return result;
    }
}
