package com.aicalendar.service;

import com.aicalendar.dto.response.AdminChatMessageResponse;
import com.aicalendar.dto.response.AdminConversationResponse;
import com.aicalendar.dto.response.PageResponse;

import java.util.List;

/**
 * 后台AI聊天记录管理服务接口
 */
public interface AdminChatService {

    /**
     * 分页查询会话列表（按对话ID分组聚合，支持按对话ID模糊搜索）
     *
     * @param keyword  对话ID关键字
     * @param pageNum  页码
     * @param pageSize 每页条数
     */
    PageResponse<AdminConversationResponse> pageConversations(String keyword, int pageNum, int pageSize);

    /**
     * 查询指定会话的全部消息（按时间升序）
     *
     * @param conversationId 对话ID
     */
    List<AdminChatMessageResponse> getMessages(String conversationId);
}
