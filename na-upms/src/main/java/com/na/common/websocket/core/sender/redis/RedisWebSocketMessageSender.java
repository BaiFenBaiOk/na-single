package com.na.common.websocket.core.sender.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.na.common.websocket.core.sender.AbstractWebSocketMessageSender;
import com.na.common.websocket.core.sender.WebSocketMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于 Redis Pub/Sub 的发送器。
 *
 * <p>只负责把消息发布到频道，由各实例上的
 * {@link RedisWebSocketMessageListener} 收到后投递给本实例持有的会话。
 * 因此发布者自身也不会重复发送。</p>
 *
 * @author pg
 */
@Slf4j
public class RedisWebSocketMessageSender implements WebSocketMessageSender {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final String channel;

    public RedisWebSocketMessageSender(StringRedisTemplate redisTemplate,
                                       ObjectMapper objectMapper,
                                       String channel) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.channel = channel;
    }

    @Override
    public void sendToSession(String sessionId, String messageType, Object content) {
        publish(new RedisWebSocketMessage()
                .setTargetType(RedisWebSocketMessage.TARGET_SESSION)
                .setSessionId(sessionId)
                .setMessageType(messageType)
                .setMessageContent(AbstractWebSocketMessageSender.toContent(objectMapper, content)));
    }

    @Override
    public void sendToUser(String userType, Long userId, String messageType, Object content) {
        publish(new RedisWebSocketMessage()
                .setTargetType(RedisWebSocketMessage.TARGET_USER)
                .setUserType(userType)
                .setUserId(userId)
                .setMessageType(messageType)
                .setMessageContent(AbstractWebSocketMessageSender.toContent(objectMapper, content)));
    }

    @Override
    public void sendToUserType(String userType, String messageType, Object content) {
        publish(new RedisWebSocketMessage()
                .setTargetType(RedisWebSocketMessage.TARGET_USER_TYPE)
                .setUserType(userType)
                .setMessageType(messageType)
                .setMessageContent(AbstractWebSocketMessageSender.toContent(objectMapper, content)));
    }

    @Override
    public void broadcast(String messageType, Object content) {
        publish(new RedisWebSocketMessage()
                .setTargetType(RedisWebSocketMessage.TARGET_ALL)
                .setMessageType(messageType)
                .setMessageContent(AbstractWebSocketMessageSender.toContent(objectMapper, content)));
    }

    private void publish(RedisWebSocketMessage message) {
        try {
            redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("[WebSocket] Redis 广播失败, channel={}", channel, e);
        }
    }
}
