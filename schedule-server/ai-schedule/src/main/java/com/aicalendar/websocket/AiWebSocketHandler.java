package com.aicalendar.websocket;

import com.aicalendar.service.AiChatService;
import org.springframework.ai.chat.client.ChatClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI聊天WebSocket处理器
 * 处理客户端WebSocket连接和消息，流式调用AI并逐段返回响应
 */
@Slf4j
@Component
public class AiWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private AiChatService aiChatService;

    // 使用 AiConfig 中预配置的 ChatClient（已绑定系统人设与 @Tool 工具）
    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 存储会话与对话ID的映射
     */
    private static final Map<String, String> sessionConversationMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String conversationId = UUID.randomUUID().toString();
        sessionConversationMap.put(session.getId(), conversationId);
        log.info("[WebSocket] 连接建立: {}, 对话ID: {}", session.getId(), conversationId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String sessionId = session.getId();
        log.info("[WebSocket] 收到消息 [{}]: {}", sessionId, payload);

        String conversationId = null;
        try {
            // 解析消息
            Map<String, String> data = objectMapper.readValue(payload, Map.class);
            String messageContent = data.get("message");
            conversationId = data.get("conversationId");

            // 如果没有提供对话ID，使用会话默认的
            if (conversationId == null || conversationId.isEmpty()) {
                conversationId = sessionConversationMap.get(sessionId);
            }

            // 保存用户消息
            aiChatService.saveChat(conversationId, "user", messageContent);

            log.info("[AI] 开始流式调用, 对话ID: {}", conversationId);

            // 流式调用AI并逐段推送
            callAiStream(session, conversationId, messageContent);

        } catch (Exception e) {
            log.error("[WebSocket] 处理消息出错 [{}]", sessionId, e);
            // 发送错误帧
            sendFrame(session, "error", "处理失败: " + e.getMessage(), conversationId);
        }
    }

    /**
     * 流式调用AI服务
     * 订阅AI流式响应，每段内容即时推送前端（chunk帧），结束后保存完整回复并发送end帧
     */
    private void callAiStream(WebSocketSession session, String conversationId, String userMessage) {
        // ChatClient 已通过 defaultTools 绑定 AiToolService，流式调用时 @Tool 方法同样生效
        Flux<String> stream = chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();

        StringBuilder fullResponse = new StringBuilder();

        stream.subscribe(
                // 逐段推送流式内容
                chunk -> {
                    fullResponse.append(chunk);
                    sendFrame(session, "chunk", chunk, conversationId);
                },
                // 调用失败：记录日志并推送降级提示
                error -> {
                    log.error("[AI] 调用失败: {}", error.getMessage());
                    sendFrame(session, "error", buildFallbackMessage(), conversationId);
                },
                // 流结束：空响应兜底，清理格式后保存完整回复并发送结束帧
                () -> {
                    String response = fullResponse.toString()
                            // 压缩3个以上连续换行为1个空行，去除首尾空白
                            .replaceAll("\\n{3,}", "\n\n")
                            .trim();
                    if (response.isEmpty()) {
                        response = "抱歉，未能获取到响应";
                        sendFrame(session, "chunk", response, conversationId);
                    }
                    aiChatService.saveChat(conversationId, "assistant", response);
                    log.info("[AI] 流式响应完成, 长度: {}", response.length());
                    sendFrame(session, "end", response, conversationId);
                }
        );
    }

    /**
     * 发送一帧JSON消息到前端
     * @param type 帧类型（chunk流式片段/end结束/error错误）
     * @param content 消息内容
     * @param conversationId 对话ID
     */
    private void sendFrame(WebSocketSession session, String type, String content, String conversationId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", type);
            data.put("content", content);
            data.put("conversationId", conversationId);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));
        } catch (Exception e) {
            log.error("[WebSocket] 发送{}帧失败: {}", type, e.getMessage());
        }
    }

    /**
     * AI服务不可用时的降级提示文案
     */
    private String buildFallbackMessage() {
        return "很抱歉，AI服务暂时不可用。我可以帮您处理日程和纪念日相关的查询：\n\n" +
                "📅 查询日程：告诉我日期，我可以帮您查看当天的日程安排\n" +
                "🎂 纪念日提醒：查询重要日期的纪念日\n" +
                "📝 制定计划：帮您创建新的日程或纪念日\n\n" +
                "请问您需要什么帮助？";
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionConversationMap.remove(session.getId());
        log.info("[WebSocket] 连接关闭: {}, 状态: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[WebSocket] 传输错误 [{}]: {}", session.getId(), exception.getMessage());
        super.handleTransportError(session, exception);
    }
}
