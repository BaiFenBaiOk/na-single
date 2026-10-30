package com.na.common.websocket.core.sender.redis;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Redis 广播的 WebSocket 消息体。
 *
 * @author pg
 */
@Data
@Accessors(chain = true)
public class RedisWebSocketMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TARGET_SESSION = "SESSION";
    public static final String TARGET_USER = "USER";
    public static final String TARGET_USER_TYPE = "USER_TYPE";
    public static final String TARGET_ALL = "ALL";

    /**
     * 目标类型：SESSION / USER / USER_TYPE / ALL
     */
    private String targetType;

    private String sessionId;

    private String userType;

    private Long userId;

    private String messageType;

    private String messageContent;
}
