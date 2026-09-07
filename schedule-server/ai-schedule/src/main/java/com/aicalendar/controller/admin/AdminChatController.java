package com.aicalendar.controller.admin;

import com.aicalendar.annotation.RequireRole;
import com.aicalendar.dto.response.AdminChatMessageResponse;
import com.aicalendar.dto.response.AdminConversationResponse;
import com.aicalendar.dto.response.PageResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.AdminChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台AI聊天记录管理控制器（仅ADMIN可访问）
 */
@RestController
@RequestMapping("/api/admin/chat")
public class AdminChatController {

    private final AdminChatService adminChatService;

    public AdminChatController(AdminChatService adminChatService) {
        this.adminChatService = adminChatService;
    }

    /**
     * 分页查询会话列表（按对话ID分组聚合）
     */
    @RequireRole({"ADMIN"})
    @GetMapping("/conversations")
    public Result<PageResponse<AdminConversationResponse>> pageConversations(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminChatService.pageConversations(keyword, page, pageSize));
    }

    /**
     * 查询指定会话的全部消息（按时间升序）
     */
    @RequireRole({"ADMIN"})
    @GetMapping("/{conversationId}/messages")
    public Result<List<AdminChatMessageResponse>> getMessages(@PathVariable String conversationId) {
        return Result.success(adminChatService.getMessages(conversationId));
    }
}
