package com.aicalendar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI聊天记录实体
 */
@Data
@TableName("ai_chat")
public class AiChat {

    /** 聊天记录ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    @TableField("user_id")
    private Long userId;

    /** 对话ID（同一对话共享） */
    @TableField("conversation_id")
    private String conversationId;

    /** 角色（user/assistant） */
    @TableField("role")
    private String role;

    /** 消息内容 */
    @TableField("content")
    private String content;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
