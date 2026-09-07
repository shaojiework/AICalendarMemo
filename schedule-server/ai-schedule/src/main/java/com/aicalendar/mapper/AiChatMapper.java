package com.aicalendar.mapper;

import com.aicalendar.dto.response.AdminConversationResponse;
import com.aicalendar.entity.AiChat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI聊天记录Mapper
 */
@Mapper
public interface AiChatMapper {

    /**
     * 插入聊天记录
     */
    int insert(AiChat aiChat);

    /**
     * 根据对话ID查询某用户的聊天记录
     */
    List<AiChat> selectByConversationId(@Param("userId") Long userId, @Param("conversationId") String conversationId);

    /**
     * 查询某用户最近的聊天记录
     */
    List<AiChat> selectRecent(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 删除某用户的对话记录
     */
    int deleteByConversationId(@Param("userId") Long userId, @Param("conversationId") String conversationId);

    /**
     * 后台：按对话ID分组统计会话列表（消息数、最后消息时间），支持按对话ID模糊搜索
     */
    List<AdminConversationResponse> selectConversationList(@Param("keyword") String keyword);

    /**
     * 后台：根据对话ID查询全部聊天记录（管理员可见所有用户数据，不做userId过滤）
     */
    List<AiChat> selectByConversationIdForAdmin(@Param("conversationId") String conversationId);

    /**
     * 后台：统计会话总数（按对话ID去重）
     */
    int countDistinctConversations();

    /**
     * 后台：查询指定时间范围内的全部聊天记录（用于趋势统计）
     */
    List<AiChat> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}