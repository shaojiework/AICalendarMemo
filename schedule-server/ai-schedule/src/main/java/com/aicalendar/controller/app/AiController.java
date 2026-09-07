package com.aicalendar.controller.app;

import com.aicalendar.dto.request.AiChatRequest;
import com.aicalendar.dto.response.Result;
import com.aicalendar.entity.AiChat;
import com.aicalendar.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI聊天控制器
 * 流式聊天由 WebSocket 通道（AiWebSocketHandler）承载，此处提供历史记录与普通聊天接口
 * 所有接口按当前登录用户隔离对话数据
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiChatService aiChatService;

    // 注入 AiConfig 中配置的 ChatClient Bean（已绑定 defaultTools 与 defaultSystem）
    @Autowired
    private ChatClient chatClient;

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
     * 删除当前登录用户的对话
     */
    @DeleteMapping("/chat")
    public Result<Void> deleteConversation(@RequestParam String conversationId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        aiChatService.deleteConversation(userId, conversationId);
        return Result.success("删除成功");
    }

    /**
     * 普通聊天接口（非流式）
     * 使用 AiConfig 配置的 ChatClient（已绑定工具与系统 prompt）
     * 通过 .toolContext(Map.of("userId", userId)) 将当前用户ID注入工具调用上下文
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AiChatRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        // 生成新的对话ID（如果没有提供）
        if (request.getConversationId() == null || request.getConversationId().isEmpty()) {
            request.setConversationId(UUID.randomUUID().toString());
        }

        // 保存用户消息
        aiChatService.saveChat(userId, request.getConversationId(), "user", request.getMessage());

        // 使用 SpringAI 生成响应，注入 userId 到工具调用上下文
        String response = chatClient.prompt()
            .user(request.getMessage())
            .toolContext(Map.of("userId", userId))
            .call()
            .content();

        // 保存AI响应
        aiChatService.saveChat(userId, request.getConversationId(), "assistant", response);
        return Result.success("查询成功", response);
    }
}
