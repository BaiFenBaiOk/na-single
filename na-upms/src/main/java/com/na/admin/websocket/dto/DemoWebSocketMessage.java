package com.na.admin.websocket.dto;

import lombok.Data;

/**
 * WebSocket 示例：客户端 -> 服务端消息。
 *
 * @author pg
 */
@Data
public class DemoWebSocketMessage {

    /**
     * 文本内容
     */
    private String text;
}
