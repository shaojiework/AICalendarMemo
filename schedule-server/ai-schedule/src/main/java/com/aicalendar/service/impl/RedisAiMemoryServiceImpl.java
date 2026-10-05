package com.aicalendar.service.impl;

import com.aicalendar.entity.AiChat;
import com.aicalendar.mapper.AiChatMapper;
import com.aicalendar.service.AiMemoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI多轮记忆Redis滑动窗口实现
 *
 * 数据结构：LIST，右进（RPUSH追加最新）左出（LTRIM只留尾部max条），窗口即队列尾部
 *   ai:hist:{userId}:{conversationId}
 *   元素JSON：{"role":"user|assistant","content":"...","ts":1730000000000}
 *
 * 为什么不用SpringAI自带MessageWindowChatMemory：它是全量存储、查询时取尾部，
 * Redis会随对话无限增长，不满足「窗口只存10轮」的存储目标；本实现写入时即刻裁剪。
 *
 * 一致性口径：DB是事实来源，Redis只是窗口缓存。
 *   写顺序：调用方先saveChat落库、再append进窗口；append失败只记日志（读侧回填自愈）
 *   读顺序：LRANGE为空（TTL过期或Redis重启）→ 查DB最近max条 → Lua批量回填
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisAiMemoryServiceImpl implements AiMemoryService {

    private final StringRedisTemplate redisTemplate;
    private final AiChatMapper aiChatMapper;
    private final ObjectMapper objectMapper;

    /** 窗口保留轮数（1轮=一问一答2条） */
    @Value("${ai.memory.window-rounds:10}")
    private int windowRounds;

    /** 窗口滑动过期（分钟）：每次读写重置，闲置超时自动淘汰 */
    @Value("${ai.memory.ttl-minutes:30}")
    private int ttlMinutes;

    /** 单条内容最大字符数：超长截断，防止一条长回答把整个窗口挤掉 */
    @Value("${ai.memory.max-content-length:4000}")
    private int maxContentLength;

    /**
     * 追加脚本：RPUSH → LTRIM只留尾部max条 → EXPIRE滑动续期，三步原子完成
     * KEYS: 1=窗口键
     * ARGV: 1=消息JSON 2=窗口最大条数 3=TTL秒
     */
    private static final RedisScript<Long> APPEND_SCRIPT = new DefaultRedisScript<>(
            "redis.call('RPUSH', KEYS[1], ARGV[1])\n" +
            "redis.call('LTRIM', KEYS[1], -tonumber(ARGV[2]), -1)\n" +
            "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3]))\n" +
            "return redis.call('LLEN', KEYS[1])", Long.class);

    /**
     * 回填脚本：批量RPUSH（按时间正序传入）→ LTRIM → EXPIRE，原子防止与并发append交错丢消息
     * KEYS: 1=窗口键
     * ARGV: 1=窗口最大条数 2=TTL秒 3..n=消息JSON（正序）
     */
    private static final RedisScript<Long> BACKFILL_SCRIPT = new DefaultRedisScript<>(
            "for i = 3, #ARGV do\n" +
            "  redis.call('RPUSH', KEYS[1], ARGV[i])\n" +
            "end\n" +
            "redis.call('LTRIM', KEYS[1], -tonumber(ARGV[1]), -1)\n" +
            "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))\n" +
            "return redis.call('LLEN', KEYS[1])", Long.class);

    @Override
    public void append(Long userId, String conversationId, String role, String content) {
        if (userId == null || conversationId == null || conversationId.isEmpty()
                || content == null || content.isEmpty()) {
            return;
        }
        // 只认user/assistant两类角色，其余（system/tool）不进窗口
        if (!"user".equals(role) && !"assistant".equals(role)) {
            return;
        }
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("role", role);
            node.put("content", truncate(content));
            node.put("ts", System.currentTimeMillis());
            redisTemplate.execute(APPEND_SCRIPT,
                    Collections.singletonList(windowKey(userId, conversationId)),
                    objectMapper.writeValueAsString(node),
                    String.valueOf(maxEntries()), String.valueOf(ttlMinutes * 60L));
        } catch (Exception e) {
            // Redis不可用不影响对话本身：DB已落库，下一次读会触发自愈回填
            log.warn("[记忆] 用户{}会话{}追加窗口失败（不阻断对话）: {}", userId, conversationId, e.getMessage());
        }
    }

    @Override
    public List<Message> loadForPrompt(Long userId, String conversationId) {
        List<Message> messages = new ArrayList<>();
        if (userId == null || conversationId == null || conversationId.isEmpty()) {
            return messages;
        }
        String key = windowKey(userId, conversationId);

        List<String> raws = redisTemplate.opsForList().range(key, 0, -1);
        if (raws == null || raws.isEmpty()) {
            // 未命中：TTL过期或Redis重启，从DB回填最近max条（新会话查不到则返回空）
            raws = backfillFromDb(userId, conversationId, key);
        } else {
            // 读到即续期，保持滑动过期语义
            redisTemplate.expire(key, ttlMinutes, TimeUnit.MINUTES);
        }

        for (String raw : raws) {
            Message message = toMessage(raw);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    @Override
    public void evict(Long userId, String conversationId) {
        if (userId == null || conversationId == null || conversationId.isEmpty()) {
            return;
        }
        redisTemplate.delete(windowKey(userId, conversationId));
    }

    /**
     * DB冷启动回填：取最近max条（DESC查出、反转为正序）经Lua原子写回窗口
     *
     * @return 正序的原始JSON列表，供调用方直接解析
     */
    private List<String> backfillFromDb(Long userId, String conversationId, String key) {
        List<AiChat> recent = aiChatMapper.selectLastByConversation(
                userId, conversationId, maxEntries());
        if (recent == null || recent.isEmpty()) {
            return Collections.emptyList();
        }
        // Mapper按时间DESC取最近N条，反转为正序（最早在前）再入窗口
        List<AiChat> ordered = new ArrayList<>(recent);
        Collections.reverse(ordered);

        List<String> raws = new ArrayList<>(ordered.size());
        try {
            for (AiChat chat : ordered) {
                if (chat.getContent() == null || chat.getContent().isEmpty()) {
                    continue;
                }
                if (!"user".equals(chat.getRole()) && !"assistant".equals(chat.getRole())) {
                    continue;
                }
                ObjectNode node = objectMapper.createObjectNode();
                node.put("role", chat.getRole());
                node.put("content", truncate(chat.getContent()));
                node.put("ts", chat.getCreatedAt() != null
                        ? chat.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        : System.currentTimeMillis());
                raws.add(objectMapper.writeValueAsString(node));
            }
            if (!raws.isEmpty()) {
                List<String> argv = new ArrayList<>();
                argv.add(String.valueOf(maxEntries()));
                argv.add(String.valueOf(ttlMinutes * 60L));
                argv.addAll(raws);
                redisTemplate.execute(BACKFILL_SCRIPT,
                        Collections.singletonList(key), argv.toArray());
                log.info("[记忆] 用户{}会话{}窗口冷启动回填{}条", userId, conversationId, raws.size());
            }
        } catch (Exception e) {
            log.warn("[记忆] 用户{}会话{}回填失败，降级为当轮无历史: {}", userId, conversationId, e.getMessage());
            return Collections.emptyList();
        }
        return raws;
    }

    /** 窗口元素JSON转SpringAI消息，解析失败或角色非法的脏数据直接跳过 */
    private Message toMessage(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            String role = node.path("role").asText();
            String content = node.path("content").asText();
            if (content.isEmpty()) {
                return null;
            }
            if ("user".equals(role)) {
                return new UserMessage(content);
            }
            if ("assistant".equals(role)) {
                return new AssistantMessage(content);
            }
            return null;
        } catch (Exception e) {
            log.warn("[记忆] 窗口元素解析失败已跳过: {}", e.getMessage());
            return null;
        }
    }

    /** 单条内容超长按上限截断，保证窗口装的是最近内容而不是被一条长文挤爆 */
    private String truncate(String content) {
        if (content.length() <= maxContentLength) {
            return content;
        }
        return content.substring(0, maxContentLength) + "...(已截断)";
    }

    /** 窗口最大条数：轮数×2（一问一答算一轮） */
    private int maxEntries() {
        return windowRounds * 2;
    }

    private String windowKey(Long userId, String conversationId) {
        return "ai:hist:" + userId + ":" + conversationId;
    }
}
