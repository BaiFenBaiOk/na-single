package com.na.common.websocket.core.sender.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.na.common.websocket.core.sender.AbstractWebSocketMessageSender;
import com.na.common.websocket.core.session.WebSocketSessionManager;

/**
 * 本地发送器：直接把消息投递给当前实例持有的会话。
 *
 * <p>仅适用于单实例部署；多实例请使用 redis 发送器。</p>
 *
 * @author pg
 */
public class LocalWebSocketMessageSender extends AbstractWebSocketMessageSender {

    public LocalWebSocketMessageSender(WebSocketSessionManager sessionManager, ObjectMapper objectMapper) {
        super(sessionManager, objectMapper);
    }
}
