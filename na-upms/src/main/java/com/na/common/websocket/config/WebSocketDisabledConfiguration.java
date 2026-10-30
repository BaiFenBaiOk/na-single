package com.na.common.websocket.config;

import com.na.common.websocket.core.sender.WebSocketMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * WebSocket 关闭时的兜底配置。
 *
 * <p>当 {@code na.websocket.enable=false} 时，{@link WebSocketAutoConfiguration}
 * 整体不生效，WebSocketMessageSender 也不会创建。如果业务侧仍有监听器或发送逻辑
 * 注入了 sender，会导致应用启动失败。</p>
 *
 * <p>这里提供一个 no-op 发送器：应用可以正常启动，但不会建立 WebSocket 连接，
 * 发送调用只记录 debug 日志。</p>
 */
@Slf4j
@AutoConfiguration(after = WebSocketAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "na.websocket", name = "enable", havingValue = "false")
public class WebSocketDisabledConfiguration {

    @Bean
    @ConditionalOnMissingBean(WebSocketMessageSender.class)
    public WebSocketMessageSender webSocketMessageSender() {
        return new WebSocketMessageSender() {
            @Override
            public void sendToSession(String sessionId, String messageType, Object content) {
                log.debug("[WebSocket] 已关闭，忽略 sendToSession type={}", messageType);
            }

            @Override
            public void sendToUser(String userType, Long userId,
                                   String messageType, Object content) {
                log.debug("[WebSocket] 已关闭，忽略 sendToUser type={} userId={}", messageType, userId);
            }

            @Override
            public void sendToUserType(String userType, String messageType, Object content) {
                log.debug("[WebSocket] 已关闭，忽略 sendToUserType type={}", messageType);
            }

            @Override
            public void broadcast(String messageType, Object content) {
                log.debug("[WebSocket] 已关闭，忽略 broadcast type={}", messageType);
            }
        };
    }
}
