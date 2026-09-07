package com.aicalendar.service.impl;

import com.aicalendar.dto.response.HomeResponse;
import com.aicalendar.entity.Memorial;
import com.aicalendar.entity.Profile;
import com.aicalendar.entity.Schedule;
import com.aicalendar.mapper.MemorialMapper;
import com.aicalendar.mapper.ProfileMapper;
import com.aicalendar.mapper.ScheduleMapper;
import com.aicalendar.service.HomeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页服务实现类（按当前登录用户聚合数据）
 */
@Service
public class HomeServiceImpl implements HomeService {

    private final ProfileMapper profileMapper;
    private final MemorialMapper memorialMapper;
    private final ScheduleMapper scheduleMapper;

    public HomeServiceImpl(ProfileMapper profileMapper,
                          MemorialMapper memorialMapper,
                          ScheduleMapper scheduleMapper) {
        this.profileMapper = profileMapper;
        this.memorialMapper = memorialMapper;
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    public HomeResponse getHomeData(Long userId) {
        HomeResponse response = new HomeResponse();

        // 获取问候语
        response.setGreeting(getGreeting());

        // 获取用户信息（profile.id即userId）
        Profile profile = profileMapper.selectById(userId);
        if (profile != null) {
            response.setUsername(profile.getUsername());
        } else {
            response.setUsername("朋友");
        }

        // 获取纪念日卡片（优先显示恋爱纪念日）
        response.setAnniversary(getAnniversaryCard(userId));

        // 获取今日日程
        response.setTodaySchedule(getTodaySchedule(userId));

        return response;
    }

    /**
     * 根据时间获取问候语
     */
    private String getGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour >= 6 && hour < 12) {
            return "早上好";
        } else if (hour >= 12 && hour < 14) {
            return "中午好";
        } else if (hour >= 14 && hour < 18) {
            return "下午好";
        } else if (hour >= 18 && hour < 22) {
            return "晚上好";
        } else {
            return "夜深了";
        }
    }

    /**
     * 获取纪念日卡片（优先显示恋爱纪念日）
     */
    private HomeResponse.MemorialCard getAnniversaryCard(Long userId) {
        List<Memorial> memorials = memorialMapper.selectByType(userId, "love");

        // 如果没有恋爱纪念日，找一个最近的重要纪念日
        if (memorials.isEmpty()) {
            memorials = memorialMapper.selectUpcoming(userId, LocalDate.now(), LocalDate.now().plusDays(30));
        }

        if (!memorials.isEmpty()) {
            Memorial memorial = memorials.get(0);
            HomeResponse.MemorialCard card = new HomeResponse.MemorialCard();
            card.setId(memorial.getId());
            card.setTitle(memorial.getName());
            card.setDate(memorial.getDate().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));

            LocalDate today = LocalDate.now();
            if (memorial.getDate().isBefore(today)) {
                card.setDaysTogether((int) ChronoUnit.DAYS.between(memorial.getDate(), today));
            } else {
                card.setDaysUntil((int) ChronoUnit.DAYS.between(today, memorial.getDate()));
            }

            return card;
        }

        return null;
    }

    /**
     * 获取今日日程
     */
    private List<HomeResponse.ScheduleItem> getTodaySchedule(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Schedule> schedules = scheduleMapper.selectByDate(userId, startOfDay, endOfDay);
        List<HomeResponse.ScheduleItem> items = new ArrayList<>();

        for (Schedule schedule : schedules) {
            HomeResponse.ScheduleItem item = new HomeResponse.ScheduleItem();
            item.setId(schedule.getId());
            item.setName(schedule.getTitle());

            // 格式化时间
            String startTime = schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            String endTime = schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            String timeStr = startTime + " - " + endTime;
            if (schedule.getLocation() != null && !schedule.getLocation().isEmpty()) {
                timeStr += " " + schedule.getLocation();
            }
            item.setTime(timeStr);

            // 设置类型标签
            String type = schedule.getType();
            item.setTag(getTypeLabel(type));

            // 设置颜色
            String color = schedule.getColor();
            item.setLineColor(color);
            item.setTagColor(color);
            item.setTagBg(lightenColor(color, 0.3));

            items.add(item);
        }

        return items;
    }

    /**
     * 获取类型标签显示文本
     */
    private String getTypeLabel(String type) {
        if (type == null) {
            return "其他";
        }
        return switch (type.toLowerCase()) {
            case "work" -> "工作";
            case "meeting" -> "会议";
            case "personal" -> "个人";
            case "normal" -> "其他";
            case "sport" -> "运动";
            case "holiday" -> "假期";
            default -> type;
        };
    }

    /**
     * 颜色变亮
     */
    private String lightenColor(String color, double percent) {
        if (color == null || !color.startsWith("#")) {
            return "#E6F4FF";
        }

        try {
            int r = Integer.parseInt(color.substring(1, 3), 16);
            int g = Integer.parseInt(color.substring(3, 5), 16);
            int b = Integer.parseInt(color.substring(5, 7), 16);

            r = Math.min(255, (int) (r + (255 - r) * percent));
            g = Math.min(255, (int) (g + (255 - g) * percent));
            b = Math.min(255, (int) (b + (255 - b) * percent));

            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception e) {
            return "#E6F4FF";
        }
    }
}
