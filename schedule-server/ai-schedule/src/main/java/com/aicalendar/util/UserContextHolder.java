package com.aicalendar.util;

/**
 * 当前登录用户ID上下文持有者
 * 用于在 AiToolService 等 @Tool 方法中获取当前调用链上的 userId
 * 调用方在 ChatClient.prompt() 前后必须成对调用 set/clear，避免线程池污染
 */
public class UserContextHolder {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    /** 设置当前登录用户ID */
    public static void set(Long userId) {
        USER_ID.set(userId);
    }

    /** 获取当前登录用户ID（未设置时返回null） */
    public static Long get() {
        return USER_ID.get();
    }

    /** 清除当前线程的用户ID上下文，防止内存泄漏与跨调用串数据 */
    public static void clear() {
        USER_ID.remove();
    }
}
