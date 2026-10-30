package com.na.common.websocket.core.session;

import com.na.common.websocket.util.WebSocketFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * WebSocketHandler 装饰器：
 * <ol>
 *     <li>连接建立时用 {@link ConcurrentWebSocketSessionDecorator} 包装会话，串行化发送</li>
 *     <li>连接建立 / 关闭时维护 {@link WebSocketSessionManager}</li>
 *     <li>把入站消息处理时的会话替换为包装后的会话，保证监听器拿到的也是并发安全会话</li>
 * </ol>
 *
 * @author pg
 */
@Slf4j
public class WebSocketSessionHandlerDecorator extends WebSocketHandlerDecorator {

    private final WebSocketSessionManager sessionManager;

    private final int sendTimeLimit;

    private final int bufferSizeLimit;

    public WebSocketSessionHandlerDecorator(WebSocketHandler delegate,
                                            WebSocketSessionManager sessionManager,
                                            int sendTimeLimit,
                                            int bufferSizeLimit) {
        super(delegate);
        this.sessionManager = sessionManager;
        this.sendTimeLimit = sendTimeLimit;
        this.bufferSizeLimit = bufferSizeLimit;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        WebSocketSession wrapped = new ConcurrentWebSocketSessionDecorator(
                session, sendTimeLimit, bufferSizeLimit);
        super.afterConnectionEstablished(wrapped);
        sessionManager.addSession(wrapped);
        log.info("[WebSocket] 连接建立, sessionId={}, userId={}, 当前连接数={}",
                wrapped.getId(),
                WebSocketFrameworkUtils.getLoginUserId(wrapped),
                sessionManager.getSessionCount());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessionManager.removeSession(session);
        super.afterConnectionClosed(session, closeStatus);
        log.info("[WebSocket] 连接关闭, sessionId={}, code={}, reason={}, 当前连接数={}",
                session.getId(), closeStatus.getCode(), closeStatus.getReason(),
                sessionManager.getSessionCount());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        WebSocketSession wrapped = sessionManager.getSession(session.getId());
        super.handleMessage(wrapped != null ? wrapped : session, message);
    }
}
