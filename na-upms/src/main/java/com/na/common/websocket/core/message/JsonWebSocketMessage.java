package com.na.common.websocket.core.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * JSON 格式的 WebSocket 消息帧。
 *
 * <pre>
 * {"type":"消息类型","content":"{\"key\":\"value\"}"}
 * </pre>
 *
 * @author pg
 */
@Data
@Accessors(chain = true)
public class JsonWebSocketMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型，用于路由到对应的 WebSocketMessageListener
     */
    private String type;

    /**
     * 消息内容，约定为 JSON 字符串
     */
    private String content;
}
