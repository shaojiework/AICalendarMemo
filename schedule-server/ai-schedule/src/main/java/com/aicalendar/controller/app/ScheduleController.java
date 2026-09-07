package com.aicalendar.controller.app;

import com.aicalendar.dto.request.ScheduleCreateRequest;
import com.aicalendar.dto.request.ScheduleUpdateRequest;
import com.aicalendar.dto.response.Result;
import com.aicalendar.dto.response.ScheduleResponse;
import com.aicalendar.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日程控制器（按当前登录用户隔离数据）
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 创建日程（绑定当前登录用户）
     */
    @PostMapping
    public Result<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ScheduleResponse response = scheduleService.createSchedule(userId, request);
        return Result.success("创建成功", response);
    }

    /**
     * 根据ID获取日程（越权返回"日程不存在"）
     */
    @GetMapping("/{id}")
    public Result<ScheduleResponse> getScheduleById(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ScheduleResponse response = scheduleService.getScheduleById(userId, id);
        return Result.success(response);
    }

    /**
     * 获取当前登录用户的所有日程
     */
    @GetMapping
    public Result<List<ScheduleResponse>> getAllSchedules(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ScheduleResponse> response = scheduleService.getAllSchedules(userId);
        return Result.success(response);
    }

    /**
     * 按日期查询当前登录用户的日程
     */
    @GetMapping("/date/{date}")
    public Result<List<ScheduleResponse>> getSchedulesByDate(@PathVariable String date, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ScheduleResponse> response = scheduleService.getSchedulesByDate(userId, date);
        return Result.success(response);
    }

    /**
     * 按类型查询当前登录用户的日程
     */
    @GetMapping("/type/{type}")
    public Result<List<ScheduleResponse>> getSchedulesByType(@PathVariable String type, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ScheduleResponse> response = scheduleService.getSchedulesByType(userId, type);
        return Result.success(response);
    }

    /**
     * 搜索当前登录用户的日程
     */
    @GetMapping("/search")
    public Result<List<ScheduleResponse>> searchSchedules(@RequestParam String keyword, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ScheduleResponse> response = scheduleService.searchSchedules(userId, keyword);
        return Result.success(response);
    }

    /**
     * 更新日程（校验归属）
     */
    @PutMapping("/{id}")
    public Result<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ScheduleResponse response = scheduleService.updateSchedule(userId, id, request);
        return Result.success("更新成功", response);
    }

    /**
     * 删除日程（校验归属）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        scheduleService.deleteSchedule(userId, id);
        Result<Void> result = Result.success();
        result.setMessage("删除成功");
        return result;
    }
}
