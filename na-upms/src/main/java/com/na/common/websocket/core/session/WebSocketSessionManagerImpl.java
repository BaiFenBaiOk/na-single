package com.na.common.websocket.core.session;

import com.na.common.dto.MyAuthDto;
import com.na.common.websocket.util.WebSocketFrameworkUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认的 WebSocket 会话管理器。
 *
 * <p>维护三组并发索引：sessionId、用户类型 + 用户、用户类型。</p>
 *
 * @author pg
 */
public class WebSocketSessionManagerImpl implements WebSocketSessionManager {

    /**
     * sessionId 与 Session 映射
     */
    private final ConcurrentMap<String, WebSocketSession> idSessions = new ConcurrentHashMap<>();

    /**
     * "userType:userId" 与 Session 列表映射
     */
    private final ConcurrentMap<String, CopyOnWriteArrayList<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    /**
     * userId 与 Session 列表映射（不限制用户类型时使用）
     */
    private final ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessionsById = new ConcurrentHashMap<>();

    /**
     * userType 与 Session 列表映射
     */
    private final ConcurrentMap<String, CopyOnWriteArrayList<WebSocketSession>> userTypeSessions = new ConcurrentHashMap<>();

    @Override
    public void addSession(WebSocketSession session) {
        if (session == null) {
            return;
        }
        idSessions.put(session.getId(), session);

        MyAuthDto user = WebSocketFrameworkUtils.getLoginUser(session);
        if (user == null) {
            return;
        }
        Long userId = parseLong(user.getId());
        if (userId == null) {
            return;
        }
        String userType = normalizeUserType(user.getUserType());

        userSessions.computeIfAbsent(userKey(userType, userId), key -> new CopyOnWriteArrayList<>())
                .add(session);
        userSessionsById.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>())
                .add(session);
        userTypeSessions.computeIfAbsent(userType, key -> new CopyOnWriteArrayList<>())
                .add(session);
    }

    @Override
    public void removeSession(WebSocketSession session) {
        if (session == null) {
            return;
        }
        idSessions.remove(session.getId());

        MyAuthDto user = WebSocketFrameworkUtils.getLoginUser(session);
        if (user == null) {
            return;
        }
        Long userId = parseLong(user.getId());
        if (userId == null) {
            return;
        }
        String userType = normalizeUserType(user.getUserType());

        removeFromUserIndex(userKey(userType, userId), session);
        removeFromIndex(userSessionsById, userId, session);
        removeFromIndex(userTypeSessions, userType, session);
    }

    @Override
    public WebSocketSession getSession(String sessionId) {
        return sessionId == null ? null : idSessions.get(sessionId);
    }

    @Override
    public Collection<WebSocketSession> getSessionsByUser(String userType, Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<WebSocketSession> sessions = StringUtils.hasText(userType)
                ? userSessions.get(userKey(userType, userId))
                : userSessionsById.get(userId);
        return sessions == null ? new ArrayList<>() : new ArrayList<>(sessions);
    }

    @Override
    public Collection<WebSocketSession> getSessionsByUserType(String userType) {
        List<WebSocketSession> sessions = userTypeSessions.get(normalizeUserType(userType));
        return sessions == null ? new ArrayList<>() : new ArrayList<>(sessions);
    }

    @Override
    public Collection<WebSocketSession> getAllSessions() {
        return new ArrayList<>(idSessions.values());
    }

    @Override
    public int getSessionCount() {
        return idSessions.size();
    }

    private void removeFromUserIndex(String key, WebSocketSession session) {
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(key);
        if (sessions == null) {
            return;
        }
        removeById(sessions, session);
        if (sessions.isEmpty()) {
            userSessions.remove(key, sessions);
        }
    }

    private <K> void removeFromIndex(Map<K, CopyOnWriteArrayList<WebSocketSession>> index,
                                     K key, WebSocketSession session) {
        CopyOnWriteArrayList<WebSocketSession> sessions = index.get(key);
        if (sessions == null) {
            return;
        }
        removeById(sessions, session);
        if (sessions.isEmpty()) {
            index.remove(key, sessions);
        }
    }

    private void removeById(CopyOnWriteArrayList<WebSocketSession> sessions, WebSocketSession session) {
        sessions.removeIf(item -> item.getId().equals(session.getId()));
    }

    private String userKey(String userType, Long userId) {
        return userType + ":" + userId;
    }

    private String normalizeUserType(String userType) {
        return userType == null ? "" : userType;
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
