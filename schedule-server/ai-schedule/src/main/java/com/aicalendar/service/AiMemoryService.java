package com.aicalendar.service;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * AI多轮记忆服务：Redis滑动窗口，只保留最近N轮对话
 * DB（ai_chat表）是全量事实来源，本服务只做喂给模型的上下文窗口缓存：
 *   写入走Lua原子「追加+裁剪+续期」，并发写不会交错破坏窗口边界
 *   缓存未命中（TTL过期或Redis重启）时自动从DB回填最近N条，对用户无感
 * key按 (userId, conversationId) 双维度隔离，A用户无法借B的对话ID读到B的缓存
 */
public interface AiMemoryService {

    /**
     * 追加一条消息进窗口（超出N轮自动裁掉最早的）
     * Redis写失败只记日志、不抛出：DB已落库，下次读会触发自愈回填
     *
     * @param role user / assistant
     */
    void append(Long userId, String conversationId, String role, String content);

    /**
     * 读取窗口内全部消息并转为SpringAI消息列表（时间正序，可直接喂模型）
     * 缓存未命中时从DB回填最近N条后再返回
     */
    List<Message> loadForPrompt(Long userId, String conversationId);

    /**
     * 清除某会话的窗口缓存（删除对话时调用，防止已删内容继续喂给模型）
     */
    void evict(Long userId, String conversationId);
}
