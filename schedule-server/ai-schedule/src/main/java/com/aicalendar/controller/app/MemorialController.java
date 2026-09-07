package com.aicalendar.controller.app;

import com.aicalendar.dto.request.MemorialCreateRequest;
import com.aicalendar.dto.request.MemorialUpdateRequest;
import com.aicalendar.dto.response.MemorialResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.MemorialService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 纪念日控制器（按当前登录用户隔离数据）
 */
@RestController
@RequestMapping("/api/memorial")
public class MemorialController {

    private final MemorialService memorialService;

    public MemorialController(MemorialService memorialService) {
        this.memorialService = memorialService;
    }

    /**
     * 创建纪念日（绑定当前登录用户）
     */
    @PostMapping
    public Result<MemorialResponse> createMemorial(@Valid @RequestBody MemorialCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        MemorialResponse response = memorialService.createMemorial(userId, request);
        return Result.success("创建成功", response);
    }

    /**
     * 根据ID获取纪念日（越权返回"纪念日不存在"）
     */
    @GetMapping("/{id}")
    public Result<MemorialResponse> getMemorialById(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        MemorialResponse response = memorialService.getMemorialById(userId, id);
        return Result.success(response);
    }

    /**
     * 获取当前登录用户的所有纪念日
     */
    @GetMapping
    public Result<List<MemorialResponse>> getAllMemorials(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<MemorialResponse> response = memorialService.getAllMemorials(userId);
        return Result.success(response);
    }

    /**
     * 按类型查询当前登录用户的纪念日
     */
    @GetMapping("/type/{type}")
    public Result<List<MemorialResponse>> getMemorialsByType(@PathVariable String type, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<MemorialResponse> response = memorialService.getMemorialsByType(userId, type);
        return Result.success(response);
    }

    /**
     * 获取当前登录用户的生日列表
     */
    @GetMapping("/birthday")
    public Result<List<MemorialResponse>> getBirthdays(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<MemorialResponse> response = memorialService.getBirthdays(userId);
        return Result.success(response);
    }

    /**
     * 更新纪念日（校验归属）
     */
    @PutMapping("/{id}")
    public Result<MemorialResponse> updateMemorial(
            @PathVariable Long id,
            @Valid @RequestBody MemorialUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        MemorialResponse response = memorialService.updateMemorial(userId, id, request);
        return Result.success("更新成功", response);
    }

    /**
     * 删除纪念日（校验归属）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMemorial(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        memorialService.deleteMemorial(userId, id);
        Result<Void> result = Result.success();
        result.setMessage("删除成功");
        return result;
    }
}
