package com.aicalendar.service.impl;

import com.aicalendar.dto.response.AdminChatMessageResponse;
import com.aicalendar.dto.response.AdminConversationResponse;
import com.aicalendar.dto.response.PageResponse;
import com.aicalendar.entity.AiChat;
import com.aicalendar.mapper.AiChatMapper;
import com.aicalendar.service.AdminChatService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台AI聊天记录管理服务实现：会话分页、会话消息查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminChatServiceImpl implements AdminChatService {

    private final AiChatMapper aiChatMapper;

    @Override
    public PageResponse<AdminConversationResponse> pageConversations(String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AdminConversationResponse> pageList = aiChatMapper.selectConversationList(keyword);
        PageInfo<AdminConversationResponse> pageInfo = new PageInfo<>(pageList);
        return PageResponse.of(pageInfo);
    }

    @Override
    public List<AdminChatMessageResponse> getMessages(String conversationId) {
        List<AiChat> chats = aiChatMapper.selectByConversationId(conversationId);
        return chats.stream().map(this::toResponse).toList();
    }

    /**
     * 实体转后台聊天消息响应（仅保留角色/内容/时间）
     */
    private AdminChatMessageResponse toResponse(AiChat chat) {
        AdminChatMessageResponse response = new AdminChatMessageResponse();
        response.setRole(chat.getRole());
        response.setContent(chat.getContent());
        response.setCreatedAt(chat.getCreatedAt());
        return response;
    }
}
