package com.aicalendar.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台AI会话列表项响应对象（ai_chat 按 conversation_id 聚合）
 */
@Data
public class AdminConversationResponse {

    /** 对话ID */
    private String conversationId;

    /** 消息条数 */
    private Integer messageCount;

    /** 最后一条消息时间 */
    private LocalDateTime lastTime;
}
