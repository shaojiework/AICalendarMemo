package com.aicalendar.service;

import com.aicalendar.entity.AiChat;

import java.util.List;

/**
 * AI聊天服务接口
 */
public interface AiChatService {
    
    /**
     * 保存聊天记录
     */
    void saveChat(String conversationId, String role, String content);
    
    /**
     * 获取对话历史
     */
    List<AiChat> getConversationHistory(String conversationId);
    
    /**
     * 获取最近聊天记录
     */
    List<AiChat> getRecentChats(int limit);
    
    /**
     * 删除对话
     */
    void deleteConversation(String conversationId);
}