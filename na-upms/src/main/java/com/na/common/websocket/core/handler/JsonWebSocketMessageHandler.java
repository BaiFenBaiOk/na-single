package com.na.common.websocket.core.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.na.common.dto.MyAuthDto;
import com.na.common.websocket.core.listener.WebSocketMessageListener;
import com.na.common.websocket.core.message.JsonWebSocketMessage;
import com.na.common.websocket.core.security.WebSocketAuthService;
import com.na.common.websocket.util.WebSocketFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JSON 格式的 WebSocket 消息处理器。
 *
 * <p>根据 {@link JsonWebSocketMessage#getType()} 把消息分发到对应的
 * {@link WebSocketMessageListener}；同时处理心跳、错误帧与 token 周期复核。</p>
 *
 * @author pg
 */
@Slf4j
public class JsonWebSocketMessageHandler extends TextWebSocketHandler {

    /** 认证过期关闭码，前端据此停止重连并跳转登录 */
    public static final int CLOSE_CODE_AUTH_EXPIRED = 4001;

    private static final String TYPE_PING = "ping";
    private static final String TYPE_PONG = "pong";
    private static final String TYPE_ERROR = "error";

    private final Map<String, WebSocketMessageListener<Object>> listeners = new HashMap<>();

    private final WebSocketAuthService authService;

    private final ObjectMapper objectMapper;

    private final long authRecheckIntervalMs;

    @SuppressWarnings("unchecked")
    public JsonWebSocketMessageHandler(List<? extends WebSocketMessageListener<?>> listenerList,
                                       WebSocketAuthService authService,
                                       ObjectMapper objectMapper,
                                       long authRecheckIntervalMs) {
        if (listenerList != null) {
            listenerList.forEach(listener ->
                    listeners.put(listener.getType(), (WebSocketMessageListener<Object>) listener));
        }
        this.authService = authService;
        this.objectMapper = objectMapper;
        this.authRecheckIntervalMs = authRecheckIntervalMs;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        if (!StringUtils.hasText(payload)) {
            return;
        }

        // 1. 原始文本心跳（兼容非 JSON 客户端）；心跳同样参与 token 周期复核
        if (TYPE_PING.equals(payload)) {
            if (!recheckAuth(session)) {
                closeForAuthExpired(session);
                return;
            }
            sendText(session, TYPE_PONG);
            return;
        }

        // 2. 解析 JSON 消息帧
        JsonWebSocketMessage jsonMessage;
        try {
            jsonMessage = objectMapper.readValue(payload, JsonWebSocketMessage.class);
        } catch (Exception e) {
            log.debug("[WebSocket] 消息不是合法 JSON, sessionId={}, payload={}", session.getId(), payload);
            sendError(session, "invalid message format");
            return;
        }
        if (jsonMessage == null || !StringUtils.hasText(jsonMessage.getType())) {
            sendError(session, "message type is required, expected {\"type\":\"xxx\",\"content\":\"...\"}");
            return;
        }

        // 3. JSON 心跳；心跳同样参与 token 周期复核
        if (TYPE_PING.equals(jsonMessage.getType())) {
            if (!recheckAuth(session)) {
                closeForAuthExpired(session);
                return;
            }
            sendJson(session, new JsonWebSocketMessage().setType(TYPE_PONG));
            return;
        }

        // 4. 周期复核 token（登出 / 过期后最长一个复核周期内断开）
        if (!recheckAuth(session)) {
            closeForAuthExpired(session);
            return;
        }

        // 5. 按 type 分发
        WebSocketMessageListener<Object> listener = listeners.get(jsonMessage.getType());
        if (listener == null) {
            log.warn("[WebSocket] 未找到消息监听器, sessionId={}, type={}",
                    session.getId(), jsonMessage.getType());
            sendError(session, "unknown message type: " + jsonMessage.getType());
            return;
        }
        try {
            Object content = resolveContent(listener, jsonMessage.getContent());
            MyAuthDto loginUser = WebSocketFrameworkUtils.getLoginUser(session);
            // 恢复 SecurityContext，使监听器内 TokenUtil.userId()/isDefAuth() 可用
            WebSocketFrameworkUtils.runAs(loginUser, () -> listener.onMessage(session, content));
        } catch (Exception e) {
            log.error("[WebSocket] 消息处理异常, sessionId={}, type={}, payload={}",
                    session.getId(), jsonMessage.getType(), payload, e);
            sendError(session, "internal error");
        }
    }

    /**
     * 按监听器泛型把 content 反序列化为目标类型；String 类型直接透传
     */
    private Object resolveContent(WebSocketMessageListener<Object> listener, String content) throws IOException {
        Class<?> targetType = GenericTypeResolver.resolveTypeArgument(
                listener.getClass(), WebSocketMessageListener.class);
        if (targetType == null || Objects.equals(targetType, String.class)
                || Objects.equals(targetType, Object.class)) {
            return content;
        }
        if (!StringUtils.hasText(content)) {
            return null;
        }
        return objectMapper.readValue(content, targetType);
    }

    /**
     * token 周期复核：距上次复核未超过间隔时直接放行
     */
    private boolean recheckAuth(WebSocketSession session) {
        long now = System.currentTimeMillis();
        if (authRecheckIntervalMs > 0) {
            Object lastCheck = session.getAttributes().get(WebSocketFrameworkUtils.ATTRIBUTE_AUTH_CHECK_TIME);
            if (lastCheck instanceof Long && now - (Long) lastCheck < authRecheckIntervalMs) {
                return true;
            }
        }
        MyAuthDto loginUser = authService.authenticate(WebSocketFrameworkUtils.getToken(session));
        if (loginUser == null) {
            return false;
        }
        session.getAttributes().put(WebSocketFrameworkUtils.ATTRIBUTE_LOGIN_USER, loginUser);
        session.getAttributes().put(WebSocketFrameworkUtils.ATTRIBUTE_AUTH_CHECK_TIME, now);
        return true;
    }

    private void closeForAuthExpired(WebSocketSession session) {
        log.info("[WebSocket] token 已失效，关闭连接, sessionId={}", session.getId());
        closeQuietly(session, new CloseStatus(CLOSE_CODE_AUTH_EXPIRED, "AUTH_EXPIRED"));
    }

    private void sendError(WebSocketSession session, String content) {
        sendJson(session, new JsonWebSocketMessage().setType(TYPE_ERROR).setContent(content));
    }

    private void sendJson(WebSocketSession session, JsonWebSocketMessage message) {
        try {
            sendText(session, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.warn("[WebSocket] 发送消息失败, sessionId={}", session.getId(), e);
        }
    }

    private void sendText(WebSocketSession session, String text) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            log.warn("[WebSocket] 发送文本消息失败, sessionId={}", session.getId(), e);
        }
    }

    private void closeQuietly(WebSocketSession session, CloseStatus closeStatus) {
        try {
            if (session.isOpen()) {
                session.close(closeStatus);
            }
        } catch (IOException e) {
            log.debug("[WebSocket] 关闭连接失败, sessionId={}", session.getId(), e);
        }
    }
}
