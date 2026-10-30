package com.na.common.websocket.core.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.na.common.websocket.core.message.JsonWebSocketMessage;
import com.na.common.websocket.core.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.SessionLimitExceededException;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * 发送器抽象实现，统一负责会话查询、消息序列化与异常处理。
 *
 * @author pg
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWebSocketMessageSender implements WebSocketMessageSender {

    protected final WebSocketSessionManager sessionManager;

    protected final ObjectMapper objectMapper;

    @Override
    public void sendToSession(String sessionId, String messageType, Object content) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        WebSocketSession session = sessionManager.getSession(sessionId);
        if (session != null) {
            doSend(Collections.singletonList(session), messageType, toContent(objectMapper, content));
        }
    }

    @Override
    public void sendToUser(String userType, Long userId, String messageType, Object content) {
        doSend(sessionManager.getSessionsByUser(userType, userId),
                messageType, toContent(objectMapper, content));
    }

    @Override
    public void sendToUserType(String userType, String messageType, Object content) {
        doSend(sessionManager.getSessionsByUserType(userType),
                messageType, toContent(objectMapper, content));
    }

    @Override
    public void broadcast(String messageType, Object content) {
        doSend(sessionManager.getAllSessions(), messageType, toContent(objectMapper, content));
    }

    /**
     * 执行发送
     */
    protected void doSend(Collection<WebSocketSession> sessions, String messageType, String messageContent) {
        if (sessions == null || sessions.isEmpty()) {
            log.debug("[WebSocket] 未匹配到会话, type={}", messageType);
            return;
        }
        String payload;
        try {
            payload = objectMapper.writeValueAsString(
                    new JsonWebSocketMessage().setType(messageType).setContent(messageContent));
        } catch (Exception e) {
            log.error("[WebSocket] 消息序列化失败, type={}", messageType, e);
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (SessionLimitExceededException e) {
                log.warn("[WebSocket] 会话发送超限，关闭连接, sessionId={}", session.getId());
                closeQuietly(session);
            } catch (IOException e) {
                log.warn("[WebSocket] 发送消息失败, sessionId={}", session.getId(), e);
            }
        }
    }

    /**
     * content 归一化：CharSequence 原样使用，其他对象序列化为 JSON
     */
    public static String toContent(ObjectMapper objectMapper, Object content) {
        if (content == null) {
            return null;
        }
        if (content instanceof CharSequence) {
            return content.toString();
        }
        try {
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            throw new IllegalArgumentException("WebSocket 消息内容序列化失败", e);
        }
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            log.debug("[WebSocket] 关闭连接失败, sessionId={}", session.getId(), e);
        }
    }
}
