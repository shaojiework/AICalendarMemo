package com.aicalendar.interceptor;

import com.aicalendar.common.TokenStatus;
import com.aicalendar.service.TokenService;
import com.aicalendar.util.JwtPayload;
import com.aicalendar.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * AI WebSocket 握手鉴权拦截器（双令牌模式）
 * 从握手URL的token参数解析访问令牌，校验类型、登出黑名单与有效期
 * 过期返回401后由前端刷新令牌再重连；校验失败同样拒绝握手（401）
 */
@Slf4j
@Component
public class AiWebSocketAuthInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从 URL query 解析 token 参数
        String token = extractTokenFromQuery(request.getURI());
        if (token == null) {
            log.warn("[WebSocket] 握手拒绝：缺少token");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 只接受访问令牌：刷新令牌密钥不同，验签阶段即被拒绝
        JwtPayload payload = jwtUtil.verifyAccessToken(token);
        if (payload.getStatus() == TokenStatus.EXPIRED) {
            log.warn("[WebSocket] 握手拒绝：token已过期，等待前端刷新后重连");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        if (!payload.isValid()) {
            log.warn("[WebSocket] 握手拒绝：token无效");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 登出后被拉黑的访问令牌不允许建立新的WebSocket会话
        if (tokenService.isAccessTokenBlacklisted(payload.getJti())) {
            log.warn("[WebSocket] 握手拒绝：token已登出");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Long userId = payload.getUserId();
        if (userId == null) {
            log.warn("[WebSocket] 握手拒绝：token中缺少userId");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 写入 attributes，后续 AiWebSocketHandler 可直接读取
        attributes.put("userId", userId);
        attributes.put("role", payload.getRole());
        log.info("[WebSocket] 握手成功：userId={}, role={}", userId, payload.getRole());
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
