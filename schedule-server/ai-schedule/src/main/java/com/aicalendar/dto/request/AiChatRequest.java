package com.aicalendar.dto.request;

/**
 * AI聊天请求DTO
 */
public class AiChatRequest {
    
    /**
     * 用户输入内容
     */
    private String message;
    
    /**
     * 对话ID（可选，用于上下文对话）
     */
    private String conversationId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}