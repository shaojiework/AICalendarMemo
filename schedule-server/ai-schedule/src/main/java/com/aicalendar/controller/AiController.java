package com.aicalendar.controller;

import com.aicalendar.dto.request.AiChatRequest;
import com.aicalendar.dto.response.Result;
import com.aicalendar.entity.AiChat;
import com.aicalendar.service.AiChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AI聊天控制器
 * 流式聊天由 WebSocket 通道（AiWebSocketHandler）承载，此处提供历史记录与普通聊天接口
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    /**
     * 获取对话历史
     */
    @GetMapping("/chat/history")
    public Result<List<AiChat>> getConversationHistory(@RequestParam String conversationId) {
        List<AiChat> history = aiChatService.getConversationHistory(conversationId);
        return Result.success("查询成功", history);
    }

    /**
     * 获取最近聊天记录
     */
    @GetMapping("/chat/recent")
    public Result<List<AiChat>> getRecentChats(@RequestParam(defaultValue = "20") int limit) {
        List<AiChat> recent = aiChatService.getRecentChats(limit);
        return Result.success("查询成功", recent);
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/chat")
    public Result<Void> deleteConversation(@RequestParam String conversationId) {
        aiChatService.deleteConversation(conversationId);
        return Result.success("删除成功");
    }

    /**
     * 普通聊天接口（非流式）
     * 使用 SpringAI 实现
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AiChatRequest request) {
        // 生成新的对话ID（如果没有提供）
        if (request.getConversationId() == null || request.getConversationId().isEmpty()) {
            request.setConversationId(UUID.randomUUID().toString());
        }
        
        // 保存用户消息
        aiChatService.saveChat(request.getConversationId(), "user", request.getMessage());
        
        // 使用 SpringAI 生成响应
        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
            .user(request.getMessage())
            .call()
            .content();
        
        // 保存AI响应
        aiChatService.saveChat(request.getConversationId(), "assistant", response);
        
        return Result.success("查询成功", response);
    }
}