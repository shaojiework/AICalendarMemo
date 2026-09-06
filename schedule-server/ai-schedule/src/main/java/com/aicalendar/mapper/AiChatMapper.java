package com.aicalendar.mapper;

import com.aicalendar.entity.AiChat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * 根据对话ID查询聊天记录
     */
    List<AiChat> selectByConversationId(@Param("conversationId") String conversationId);
    
    /**
     * 查询最近的聊天记录
     */
    List<AiChat> selectRecent(@Param("limit") int limit);
    
    /**
     * 删除对话记录
     */
    int deleteByConversationId(@Param("conversationId") String conversationId);
}