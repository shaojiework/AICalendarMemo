package com.aicalendar.dto.response;

import java.util.List;

/**
 * 首页聚合响应DTO
 */
public class HomeResponse {

    private String greeting;

    private String username;

    private MemorialCard anniversary;

    private List<ScheduleItem> todaySchedule;

    public HomeResponse() {
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public MemorialCard getAnniversary() {
        return anniversary;
    }

    public void setAnniversary(MemorialCard anniversary) {
        this.anniversary = anniversary;
    }

    public List<ScheduleItem> getTodaySchedule() {
        return todaySchedule;
    }

    public void setTodaySchedule(List<ScheduleItem> todaySchedule) {
        this.todaySchedule = todaySchedule;
    }

    /**
     * 纪念日卡片数据
     */
    public static class MemorialCard {
        private Long id;
        private String title;
        private String date;
        private Integer daysTogether;
        private Integer daysUntil;

        public MemorialCard() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getDaysTogether() {
            return daysTogether;
        }

        public void setDaysTogether(Integer daysTogether) {
            this.daysTogether = daysTogether;
        }

        public Integer getDaysUntil() {
            return daysUntil;
        }

        public void setDaysUntil(Integer daysUntil) {
            this.daysUntil = daysUntil;
        }
    }

    /**
     * 日程项数据
     */
    public static class ScheduleItem {
        private Long id;
        private String name;
        private String time;
        private String tag;
        private String lineColor;
        private String tagBg;
        private String tagColor;

        public ScheduleItem() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getLineColor() {
            return lineColor;
        }

        public void setLineColor(String lineColor) {
            this.lineColor = lineColor;
        }

        public String getTagBg() {
            return tagBg;
        }

        public void setTagBg(String tagBg) {
            this.tagBg = tagBg;
        }

        public String getTagColor() {
            return tagColor;
        }

        public void setTagColor(String tagColor) {
            this.tagColor = tagColor;
        }
    }
}