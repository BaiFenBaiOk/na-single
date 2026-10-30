package com.na.common.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket 配置项。
 *
 * <p>配置前缀：{@code na.websocket}</p>
 *
 * @author pg
 */
@Data
@ConfigurationProperties(prefix = "na.websocket")
public class WebSocketProperties {

    /**
     * 是否启用 WebSocket 能力
     */
    private boolean enable = true;

    /**
     * 服务内 WebSocket 连接路径；前端经代理访问 /na-upms/ws
     */
    private String path = "/ws";

    /**
     * 消息发送器类型：local（单机）/ redis（多实例广播）
     */
    private String senderType = "local";

    /**
     * 允许的跨域来源，默认全部允许
     */
    private String[] allowedOrigins = {"*"};

    /**
     * 单次发送时间限制（毫秒），超过后关闭会话
     */
    private int sendTimeLimit = 5000;

    /**
     * 单会话发送缓冲区上限（字节）
     */
    private int bufferSizeLimit = 102400;

    /**
     * 入站消息的 token 复核间隔（毫秒），0 表示每次消息都复核
     */
    private long authRecheckIntervalMs = 60000;

    /**
     * Redis 广播频道
     */
    private String redisChannel = "na:websocket:broadcast";
}
