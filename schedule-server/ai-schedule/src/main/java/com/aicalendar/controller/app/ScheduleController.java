package com.aicalendar.controller.app;

import com.aicalendar.dto.request.ScheduleCreateRequest;
import com.aicalendar.dto.request.ScheduleUpdateRequest;
import com.aicalendar.dto.response.Result;
import com.aicalendar.dto.response.ScheduleResponse;
import com.aicalendar.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日程控制器
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 创建日程
     */
    @PostMapping
    public Result<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleCreateRequest request) {
        ScheduleResponse response = scheduleService.createSchedule(request);
        return Result.success("创建成功", response);
    }

    /**
     * 根据ID获取日程
     */
    @GetMapping("/{id}")
    public Result<ScheduleResponse> getScheduleById(@PathVariable Long id) {
        ScheduleResponse response = scheduleService.getScheduleById(id);
        return Result.success(response);
    }

    /**
     * 获取所有日程
     */
    @GetMapping
    public Result<List<ScheduleResponse>> getAllSchedules() {
        List<ScheduleResponse> response = scheduleService.getAllSchedules();
        return Result.success(response);
    }

    /**
     * 按日期查询日程
     */
    @GetMapping("/date/{date}")
    public Result<List<ScheduleResponse>> getSchedulesByDate(@PathVariable String date) {
        List<ScheduleResponse> response = scheduleService.getSchedulesByDate(date);
        return Result.success(response);
    }

    /**
     * 按类型查询日程
     */
    @GetMapping("/type/{type}")
    public Result<List<ScheduleResponse>> getSchedulesByType(@PathVariable String type) {
        List<ScheduleResponse> response = scheduleService.getSchedulesByType(type);
        return Result.success(response);
    }

    /**
     * 搜索日程
     */
    @GetMapping("/search")
    public Result<List<ScheduleResponse>> searchSchedules(@RequestParam String keyword) {
        List<ScheduleResponse> response = scheduleService.searchSchedules(keyword);
        return Result.success(response);
    }

    /**
     * 更新日程
     */
    @PutMapping("/{id}")
    public Result<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        ScheduleResponse response = scheduleService.updateSchedule(id, request);
        return Result.success("更新成功", response);
    }

    /**
     * 删除日程
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        Result<Void> result = Result.success();
        result.setMessage("删除成功");
        return result;
    }
}
