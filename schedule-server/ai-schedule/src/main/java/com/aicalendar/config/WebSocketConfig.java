package com.aicalendar.config;

import com.aicalendar.interceptor.AiWebSocketAuthInterceptor;
import com.aicalendar.websocket.AiWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 注册 AI 聊天端点并启用握手鉴权（从 URL query 解析 token）
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AiWebSocketHandler aiWebSocketHandler;

    @Autowired
    private AiWebSocketAuthInterceptor aiWebSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，附加握手鉴权拦截器，允许跨域
        registry.addHandler(aiWebSocketHandler, "/ws/ai/chat")
                .addInterceptors(aiWebSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }
}
