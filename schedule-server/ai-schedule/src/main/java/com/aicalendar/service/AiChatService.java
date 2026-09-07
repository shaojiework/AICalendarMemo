package com.aicalendar.service;

import com.aicalendar.entity.AiChat;

import java.util.List;

/**
 * AI聊天服务接口（按当前登录用户隔离数据）
 */
public interface AiChatService {

    /** 保存聊天记录（绑定userId） */
    void saveChat(Long userId, String conversationId, String role, String content);

    /** 获取某用户的对话历史 */
    List<AiChat> getConversationHistory(Long userId, String conversationId);

    /** 获取某用户最近聊天记录 */
    List<AiChat> getRecentChats(Long userId, int limit);

    /** 删除某用户的对话 */
    void deleteConversation(Long userId, String conversationId);
}
