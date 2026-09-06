package com.aicalendar.controller;

import com.aicalendar.dto.request.MemorialCreateRequest;
import com.aicalendar.dto.request.MemorialUpdateRequest;
import com.aicalendar.dto.response.MemorialResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.MemorialService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 纪念日控制器
 */
@RestController
@RequestMapping("/api/memorial")
public class MemorialController {

    private final MemorialService memorialService;

    public MemorialController(MemorialService memorialService) {
        this.memorialService = memorialService;
    }

    /**
     * 创建纪念日
     */
    @PostMapping
    public Result<MemorialResponse> createMemorial(@Valid @RequestBody MemorialCreateRequest request) {
        MemorialResponse response = memorialService.createMemorial(request);
        return Result.success("创建成功", response);
    }

    /**
     * 根据ID获取纪念日
     */
    @GetMapping("/{id}")
    public Result<MemorialResponse> getMemorialById(@PathVariable Long id) {
        MemorialResponse response = memorialService.getMemorialById(id);
        return Result.success(response);
    }

    /**
     * 获取所有纪念日
     */
    @GetMapping
    public Result<List<MemorialResponse>> getAllMemorials() {
        List<MemorialResponse> response = memorialService.getAllMemorials();
        return Result.success(response);
    }

    /**
     * 按类型查询纪念日
     */
    @GetMapping("/type/{type}")
    public Result<List<MemorialResponse>> getMemorialsByType(@PathVariable String type) {
        List<MemorialResponse> response = memorialService.getMemorialsByType(type);
        return Result.success(response);
    }

    /**
     * 获取生日列表
     */
    @GetMapping("/birthday")
    public Result<List<MemorialResponse>> getBirthdays() {
        List<MemorialResponse> response = memorialService.getBirthdays();
        return Result.success(response);
    }

    /**
     * 更新纪念日
     */
    @PutMapping("/{id}")
    public Result<MemorialResponse> updateMemorial(
            @PathVariable Long id,
            @Valid @RequestBody MemorialUpdateRequest request) {
        MemorialResponse response = memorialService.updateMemorial(id, request);
        return Result.success("更新成功", response);
    }

    /**
     * 删除纪念日
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMemorial(@PathVariable Long id) {
        memorialService.deleteMemorial(id);
        Result<Void> result = Result.success();
        result.setMessage("删除成功");
        return result;
    }
}