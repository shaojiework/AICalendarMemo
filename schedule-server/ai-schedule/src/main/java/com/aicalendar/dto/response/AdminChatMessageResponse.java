package com.aicalendar.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台AI会话消息响应对象（单条聊天消息）
 */
@Data
public class AdminChatMessageResponse {

    /** 角色（user/assistant） */
    private String role;

    /** 消息内容 */
    private String content;

    /** 发送时间 */
    private LocalDateTime createdAt;
}
