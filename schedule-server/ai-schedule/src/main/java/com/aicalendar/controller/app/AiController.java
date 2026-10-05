package com.aicalendar.controller.app;

import com.aicalendar.dto.response.Result;
import com.aicalendar.entity.AiChat;
import com.aicalendar.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI聊天控制器
 * 对话生成统一走 WebSocket 通道（AiWebSocketHandler），此处仅提供聊天记录的历史查询与删除
 * 所有接口按当前登录用户隔离对话数据
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiChatService aiChatService;

    /**
     * 获取当前登录用户的对话历史
     */
    @GetMapping("/chat/history")
    public Result<List<AiChat>> getConversationHistory(@RequestParam String conversationId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<AiChat> history = aiChatService.getConversationHistory(userId, conversationId);
        return Result.success("查询成功", history);
    }

    /**
     * 获取当前登录用户最近聊天记录
     */
    @GetMapping("/chat/recent")
    public Result<List<AiChat>> getRecentChats(@RequestParam(defaultValue = "20") int limit, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<AiChat> recent = aiChatService.getRecentChats(userId, limit);
        return Result.success("查询成功", recent);
    }

    /**
     * 删除当前登录用户的对话（同时清除Redis记忆窗口）
     */
    @DeleteMapping("/chat")
    public Result<Void> deleteConversation(@RequestParam String conversationId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        aiChatService.deleteConversation(userId, conversationId);
        return Result.success("删除成功");
    }
}
