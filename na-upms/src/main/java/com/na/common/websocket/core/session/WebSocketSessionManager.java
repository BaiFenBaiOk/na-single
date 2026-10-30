package com.na.common.websocket.core.session;

import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;

/**
 * WebSocket 会话管理器。
 *
 * @author pg
 */
public interface WebSocketSessionManager {

    /**
     * 添加会话
     */
    void addSession(WebSocketSession session);

    /**
     * 移除会话
     */
    void removeSession(WebSocketSession session);

    /**
     * 按 Session 编号获取会话
     */
    WebSocketSession getSession(String sessionId);

    /**
     * 获取指定用户类型 + 用户 ID 的所有会话
     *
     * @param userType 用户类型（00 / 03），为空表示不限制
     * @param userId   用户 ID
     */
    Collection<WebSocketSession> getSessionsByUser(String userType, Long userId);

    /**
     * 获取指定用户类型的所有会话
     *
     * @param userType 用户类型（00 / 03）
     */
    Collection<WebSocketSession> getSessionsByUserType(String userType);

    /**
     * 获取所有会话
     */
    Collection<WebSocketSession> getAllSessions();

    /**
     * 当前会话数量
     */
    int getSessionCount();
}
