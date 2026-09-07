package com.aicalendar.controller.admin;

import com.aicalendar.annotation.RequireRole;
import com.aicalendar.dto.response.AdminMemorialResponse;
import com.aicalendar.dto.response.PageResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.AdminMemorialService;
import org.springframework.web.bind.annotation.*;

/**
 * 后台纪念日管理控制器（仅ADMIN可访问）
 */
@RestController
@RequestMapping("/api/admin/memorial")
public class AdminMemorialController {

    private final AdminMemorialService adminMemorialService;

    public AdminMemorialController(AdminMemorialService adminMemorialService) {
        this.adminMemorialService = adminMemorialService;
    }

    /**
     * 分页查询纪念日列表（名称关键字/类型过滤）
     */
    @RequireRole({"ADMIN"})
    @GetMapping
    public Result<PageResponse<AdminMemorialResponse>> pageMemorials(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminMemorialService.pageMemorials(keyword, type, page, pageSize));
    }

    /**
     * 查询纪念日详情
     */
    @RequireRole({"ADMIN"})
    @GetMapping("/{id}")
    public Result<AdminMemorialResponse> getMemorialById(@PathVariable Long id) {
        return Result.success(adminMemorialService.getMemorialById(id));
    }

    /**
     * 删除纪念日
     */
    @RequireRole({"ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> deleteMemorial(@PathVariable Long id) {
        adminMemorialService.deleteMemorial(id);
        return Result.success("删除成功", null);
    }
}
