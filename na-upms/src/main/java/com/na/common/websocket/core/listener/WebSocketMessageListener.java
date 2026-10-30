package com.na.common.websocket.core.listener;

import com.na.common.websocket.core.message.JsonWebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 消息监听器。
 *
 * <p>实现类注册为 Spring Bean 后，会由 {@link com.na.common.websocket.core.handler.JsonWebSocketMessageHandler}
 * 按 {@link #getType()} 自动收集并分发消息。</p>
 *
 * @param <T> 消息内容类型；为 String 时直接接收原始 content
 * @author pg
 */
public interface WebSocketMessageListener<T> {

    /**
     * 处理客户端发来的消息
     *
     * @param session 当前连接
     * @param message 反序列化后的消息内容
     */
    void onMessage(WebSocketSession session, T message);

    /**
     * 消息类型
     *
     * @see JsonWebSocketMessage#getType()
     */
    String getType();
}
