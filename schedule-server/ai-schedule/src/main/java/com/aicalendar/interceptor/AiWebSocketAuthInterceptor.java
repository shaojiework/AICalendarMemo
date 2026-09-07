package com.aicalendar.interceptor;

import com.aicalendar.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * AI WebSocket 握手鉴权拦截器
 * 从握手 URL 的 token 参数解析 JWT，校验通过后将 userId 写入 attributes 供后续消息处理使用
 * 校验失败拒绝握手（返回 401）
 */
@Slf4j
@Component
public class AiWebSocketAuthInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从 URL query 解析 token 参数
        String token = extractTokenFromQuery(request.getURI());
        if (token == null) {
            log.warn("[WebSocket] 握手拒绝：缺少token");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 校验 token 并取 userId
        DecodedJWT jwt = jwtUtil.verifyToken(token);
        if (jwt == null) {
            log.warn("[WebSocket] 握手拒绝：token无效或已过期");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        Long userId = jwt.getClaim("userId").asLong();
        String role = jwt.getClaim("role").asString();
        if (userId == null) {
            log.warn("[WebSocket] 握手拒绝：token中缺少userId");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 写入 attributes，后续 AiWebSocketHandler 可直接读取
        attributes.put("userId", userId);
        attributes.put("role", role);
        log.info("[WebSocket] 握手成功：userId={}, role={}", userId, role);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后无需额外处理
    }

    /**
     * 从 URL query 字符串中解析 token 参数
     */
    private String extractTokenFromQuery(URI uri) {
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String param : query.split("&")) {
            int idx = param.indexOf('=');
            if (idx > 0 && "token".equals(param.substring(0, idx))) {
                return param.substring(idx + 1);
            }
        }
        return null;
    }
}
