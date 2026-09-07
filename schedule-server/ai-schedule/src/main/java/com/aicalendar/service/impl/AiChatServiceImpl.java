package com.aicalendar.service.impl;

import com.aicalendar.entity.AiChat;
import com.aicalendar.mapper.AiChatMapper;
import com.aicalendar.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI聊天服务实现类
 * 负责聊天记录的存取，AI调用由 AiWebSocketHandler 流式完成
 * 所有操作绑定当前登录用户，跨用户对话ID访问返回空列表避免暴露存在性
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private AiChatMapper aiChatMapper;

    @Override
    @Transactional
    public void saveChat(Long userId, String conversationId, String role, String content) {
        AiChat aiChat = new AiChat();
        aiChat.setUserId(userId);
        aiChat.setConversationId(conversationId);
        aiChat.setRole(role);
        aiChat.setContent(content);
        aiChat.setCreatedAt(LocalDateTime.now());
        aiChatMapper.insert(aiChat);
    }

    @Override
    public List<AiChat> getConversationHistory(Long userId, String conversationId) {
        return aiChatMapper.selectByConversationId(userId, conversationId);
    }

    @Override
    public List<AiChat> getRecentChats(Long userId, int limit) {
        return aiChatMapper.selectRecent(userId, limit);
    }

    @Override
    @Transactional
    public void deleteConversation(Long userId, String conversationId) {
        aiChatMapper.deleteByConversationId(userId, conversationId);
    }
}
