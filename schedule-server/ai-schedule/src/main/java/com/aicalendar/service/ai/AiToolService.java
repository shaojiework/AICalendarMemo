package com.aicalendar.service.ai;

import com.aicalendar.dto.request.MemorialCreateRequest;
import com.aicalendar.dto.request.ScheduleCreateRequest;
import com.aicalendar.dto.response.MemorialResponse;
import com.aicalendar.dto.response.ScheduleResponse;
import com.aicalendar.service.MemorialService;
import com.aicalendar.service.ScheduleService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AI 工具服务
 * 提供 AI 可调用的业务方法，使用 @Tool 注解标记
 * 通过 SpringAI ToolContext 机制接收当前登录用户ID（框架自动注入，跨线程可靠）
 * 调用方在 ChatClient.prompt().toolContext(Map.of("userId", userId)) 处传入
 */
@Slf4j
@Service
public class AiToolService {

    @Tool(
        description = "查询指定日期的日程安排，返回该日期的所有日程列表，日期格式为 yyyy-MM-dd"
    )
    public String getSchedulesByDate(String date, ToolContext toolContext) {
        Long userId = extractUserId(toolContext);
        log.info("[工具调用] getSchedulesByDate(userId={}, date={})", userId, date);

        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDate(userId, date);

        log.info("[工具调用] 查询结果：{} 条", schedules.size());
        if (schedules.isEmpty()) {
            return "该日期暂无日程安排";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("该日期共有 ").append(schedules.size()).append(" 个日程：\n");
        for (ScheduleResponse schedule : schedules) {
            sb.append("- ").append(schedule.getTitle())
              .append("（").append(schedule.getStartTime())
              .append(" - ").append(schedule.getEndTime()).append("）\n");
        }
        return sb.toString();
    }

    @Tool(
        description = "创建新的日程，需要提供标题、开始时间、结束时间等参数。时间格式为 yyyy-MM-dd HH:mm:ss。" +
                "type类型可选值：work(工作)、personal(个人)、meeting(会议)、normal(其他)，默认normal。" +
                "description为可选的日程描述，color为可选的颜色（不传时按类型自动配色）"
    )
    public String createSchedule(String title, String startTime, String endTime, String location, String type, String color, String description, ToolContext toolContext) {
        Long userId = extractUserId(toolContext);
        log.info("[工具调用] createSchedule(userId={}, title={}, startTime={}, endTime={})", userId, title, startTime, endTime);

        try {
            ScheduleCreateRequest request = new ScheduleCreateRequest();
            request.setTitle(title);
            request.setStartTime(LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            request.setEndTime(LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            request.setLocation(location != null ? location : "");
            // 类型统一转换为数据库约定的 value（work/personal/meeting/normal）
            String typeValue = convertScheduleType(type);
            request.setType(typeValue);
            // 颜色规则与手动创建一致：未传时按类型取色板，传入时转换中文色名
            request.setColor(color != null && !color.trim().isEmpty()
                    ? convertColor(color)
                    : getScheduleTypeColor(typeValue));
            request.setDescription(description != null ? description : "");

            scheduleService.createSchedule(userId, request);
            log.info("[工具调用] 日程创建成功");
            return "日程创建成功：" + title;
        } catch (Exception e) {
            log.error("[工具调用] 日程创建失败: {}", e.getMessage());
            return "日程创建失败：" + e.getMessage();
        }
    }

    @Tool(
        description = "查询所有纪念日列表，返回所有纪念日的详细信息"
    )
    public String getAllMemorials(ToolContext toolContext) {
        Long userId = extractUserId(toolContext);
        List<MemorialResponse> memorials = memorialService.getAllMemorials(userId);

        if (memorials.isEmpty()) {
            return "暂无纪念日记录";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("共有 ").append(memorials.size()).append(" 个纪念日：\n");
        for (MemorialResponse memorial : memorials) {
            sb.append("- ").append(memorial.getName())
              .append("（").append(memorial.getDate()).append("）\n");
        }
        return sb.toString();
    }

    @Tool(
        description = "创建新的纪念日，需要提供名称、日期、类型等参数。日期格式为 yyyy-MM-dd。" +
                "type类型可选值：normal(普通)、love(恋爱)、marriage(结婚)、birthday(生日)、other(其他)，默认normal"
    )
    public String createMemorial(String name, String date, String type, String description, String color, ToolContext toolContext) {
        Long userId = extractUserId(toolContext);
        log.info("[工具调用] createMemorial(userId={}, name={}, date={}, color={})", userId, name, date, color);

        try {
            MemorialCreateRequest request = new MemorialCreateRequest();
            request.setName(name);
            request.setDate(LocalDate.parse(date));
            // 类型统一转换为数据库约定的 value（normal/love/marriage/birthday/other）
            request.setType(convertMemorialType(type));
            request.setDescription(description != null ? description : "");
            // 颜色规则与手动创建一致：未传时默认粉色，传入时转换中文色名
            request.setColor(color != null && !color.trim().isEmpty() ? convertColor(color) : "#FF7B9C");
            // 头像与手动创建默认保持一致（空字符串）
            request.setAvatar("");
            request.setIsYearly(1);

            memorialService.createMemorial(userId, request);
            log.info("[工具调用] 纪念日创建成功");
            return "纪念日创建成功：" + name;
        } catch (Exception e) {
            log.error("[工具调用] 纪念日创建失败: {}", e.getMessage());
            return "纪念日创建失败：" + e.getMessage();
        }
    }

    @Tool(
        description = "查询即将到来的纪念日，返回最近的纪念日列表"
    )
    public String getUpcomingMemorials(ToolContext toolContext) {
        Long userId = extractUserId(toolContext);
        List<MemorialResponse> memorials = memorialService.getUpcomingMemorials(userId);

        if (memorials.isEmpty()) {
            return "暂无即将到来的纪念日";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("即将到来的纪念日：\n");
        for (MemorialResponse memorial : memorials) {
            sb.append("- ").append(memorial.getName())
              .append("（").append(memorial.getDate()).append("）\n");
        }
        return sb.toString();
    }

    @Tool(
        description = "获取当前日期，格式为 yyyy-MM-dd"
    )
    public String getCurrentDate() {
        return LocalDate.now().toString();
    }

    @Tool(
        description = "获取当前时间，格式为 yyyy-MM-dd HH:mm:ss"
    )
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 从 ToolContext 中提取当前登录用户ID
     * 调用方通过 ChatClient.prompt().toolContext(Map.of("userId", userId)) 传入
     */
    private Long extractUserId(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            log.warn("[工具调用] ToolContext 为空，无法获取 userId");
            throw new IllegalStateException("无法获取当前登录用户ID");
        }
        Object userIdVal = toolContext.getContext().get("userId");
        if (userIdVal == null) {
            log.warn("[工具调用] ToolContext 中未找到 userId");
            throw new IllegalStateException("无法获取当前登录用户ID");
        }
        if (userIdVal instanceof Long) {
            return (Long) userIdVal;
        }
        // 兼容 Integer 等数字类型
        return ((Number) userIdVal).longValue();
    }

    /**
     * 将日程类型转换为数据库约定的 value
     * 约定与前端 typeOptions 一致：work=工作、personal=个人、meeting=会议、normal=其他
     */
    private String convertScheduleType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "normal"; // 默认其他
        }

        String normalized = type.trim();

        // 已是约定 value，直接返回
        switch (normalized) {
            case "work":
            case "personal":
            case "meeting":
            case "normal":
                return normalized;
            // 中文类型名映射
            case "工作":
                return "work";
            case "个人":
                return "personal";
            case "会议":
                return "meeting";
            case "其他":
            case "日常":
                return "normal";
            default:
                // 未知类型兜底为其他
                return "normal";
        }
    }

    /**
     * 将纪念日类型转换为数据库约定的 value
     * 约定与前端 typeValues 一致：normal=普通、love=恋爱、marriage=结婚、birthday=生日、other=其他
     */
    private String convertMemorialType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "normal"; // 默认普通
        }

        String normalized = type.trim();

        // 已是约定 value，直接返回
        switch (normalized) {
            case "normal":
            case "love":
            case "marriage":
            case "birthday":
            case "other":
                return normalized;
            // 中文类型名映射
            case "普通":
                return "normal";
            case "恋爱":
            case "爱情":
            case "相识":
                return "love";
            case "结婚":
            case "婚礼":
            case "婚姻":
                return "marriage";
            case "生日":
            case "寿辰":
                return "birthday";
            case "其他":
                return "other";
            default:
                // 未知类型兜底为其他
                return "other";
        }
    }

    /**
     * 日程类型对应的色板颜色，与前端 typeOptions 保持一致
     */
    private String getScheduleTypeColor(String type) {
        switch (type) {
            case "work":
                return "#409EFF";
            case "personal":
                return "#FF7B9C";
            case "meeting":
                return "#7940EC";
            default:
                return "#36C9A5"; // normal 其他
        }
    }

    /**
     * 将中文颜色名称转换为十六进制颜色值
     */
    private String convertColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return "#FF7B9C"; // 默认粉色，与手动创建一致
        }

        // 去除首尾空格
        String normalizedColor = color.trim();

        // 如果已经是十六进制颜色值，直接返回
        if (normalizedColor.startsWith("#") && normalizedColor.length() == 7) {
            return normalizedColor;
        }

        // 中文颜色名称映射
        switch (normalizedColor) {
            case "红色":
            case "红":
                return "#EF4444";
            case "粉色":
            case "粉":
                return "#FF7B9C";
            case "蓝色":
            case "蓝":
                return "#3B82F6";
            case "绿色":
            case "绿":
                return "#10B981";
            case "黄色":
            case "黄":
                return "#F59E0B";
            case "紫色":
            case "紫":
                return "#8B5CF6";
            case "橙色":
            case "橙":
                return "#F97316";
            case "青色":
            case "青":
                return "#06B6D4";
            case "灰色":
            case "灰":
                return "#6B7280";
            case "黑色":
            case "黑":
                return "#1F2937";
            case "白色":
            case "白":
                return "#FFFFFF";
            default:
                return "#EF4444"; // 默认红色
        }
    }

    private final ScheduleService scheduleService;
    private final MemorialService memorialService;

    public AiToolService(ScheduleService scheduleService, MemorialService memorialService) {
        this.scheduleService = scheduleService;
        this.memorialService = memorialService;
    }
}
