package com.na.common.websocket.core.sender.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.na.common.websocket.core.sender.local.LocalWebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;

/**
 * Redis 广播消息消费者：收到本实例应投递的消息后，交给本地发送器处理。
 *
 * @author pg
 */
@Slf4j
@RequiredArgsConstructor
public class RedisWebSocketMessageListener implements MessageListener {

    private final LocalWebSocketMessageSender localSender;

    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            RedisWebSocketMessage wsMessage = objectMapper.readValue(body, RedisWebSocketMessage.class);
            String targetType = wsMessage.getTargetType();
            if (targetType == null) {
                log.warn("[WebSocket] Redis 广播消息缺少目标类型: {}", body);
                return;
            }
            switch (targetType) {
                case RedisWebSocketMessage.TARGET_SESSION -> localSender.sendToSession(
                        wsMessage.getSessionId(), wsMessage.getMessageType(), wsMessage.getMessageContent());
                case RedisWebSocketMessage.TARGET_USER -> localSender.sendToUser(
                        wsMessage.getUserType(), wsMessage.getUserId(),
                        wsMessage.getMessageType(), wsMessage.getMessageContent());
                case RedisWebSocketMessage.TARGET_USER_TYPE -> localSender.sendToUserType(
                        wsMessage.getUserType(), wsMessage.getMessageType(), wsMessage.getMessageContent());
                case RedisWebSocketMessage.TARGET_ALL -> localSender.broadcast(
                        wsMessage.getMessageType(), wsMessage.getMessageContent());
                default -> log.warn("[WebSocket] 未知的 Redis 广播目标类型: {}", targetType);
            }
        } catch (Exception e) {
            log.error("[WebSocket] Redis 广播消息处理失败", e);
        }
    }
}
