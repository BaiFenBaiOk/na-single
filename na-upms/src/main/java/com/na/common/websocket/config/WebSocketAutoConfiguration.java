package com.na.common.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.na.auth.AutoAuthConfig;
import com.na.common.login.LoginSessionService;
import com.na.common.websocket.core.handler.JsonWebSocketMessageHandler;
import com.na.common.websocket.core.listener.WebSocketMessageListener;
import com.na.common.websocket.core.security.LoginUserHandshakeInterceptor;
import com.na.common.websocket.core.security.WebSocketAuthService;
import com.na.common.websocket.core.sender.WebSocketMessageSender;
import com.na.common.websocket.core.sender.local.LocalWebSocketMessageSender;
import com.na.common.websocket.core.sender.redis.RedisWebSocketMessageListener;
import com.na.common.websocket.core.sender.redis.RedisWebSocketMessageSender;
import com.na.common.websocket.core.session.WebSocketSessionHandlerDecorator;
import com.na.common.websocket.core.session.WebSocketSessionManager;
import com.na.common.websocket.core.session.WebSocketSessionManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket 自动配置。
 *
 * <p>仅在 Servlet Web 应用中生效；服务可通过 {@code na.websocket.enable=false} 关闭。</p>
 *
 * @author pg
 */
@Slf4j
@AutoConfiguration(afterName = "com.na.common.autoconfigure.NaCommonAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({WebSocketConfigurer.class, WebSocketHandler.class})
@ConditionalOnProperty(prefix = "na.websocket", name = "enable", havingValue = "true", matchIfMissing = true)
@EnableWebSocket
@EnableConfigurationProperties(WebSocketProperties.class)
public class WebSocketAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebSocketSessionManager webSocketSessionManager() {
        return new WebSocketSessionManagerImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebSocketAuthService webSocketAuthService(LoginSessionService loginSessionService) {
        return new WebSocketAuthService(loginSessionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginUserHandshakeInterceptor loginUserHandshakeInterceptor(WebSocketAuthService authService) {
        return new LoginUserHandshakeInterceptor(authService);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebSocketHandler webSocketHandler(WebSocketSessionManager sessionManager,
                                             ApplicationContext applicationContext,
                                             WebSocketAuthService authService,
                                             ObjectMapper objectMapper,
                                             WebSocketProperties properties) {
        // 主动从容器收集监听器，避免"没有任何监听器时集合注入失败"的边界问题
        List<WebSocketMessageListener<?>> listenerList = new ArrayList<>();
        for (WebSocketMessageListener<?> listener
                : applicationContext.getBeansOfType(WebSocketMessageListener.class).values()) {
            listenerList.add(listener);
        }
        JsonWebSocketMessageHandler messageHandler = new JsonWebSocketMessageHandler(
                listenerList, authService, objectMapper, properties.getAuthRecheckIntervalMs());
        return new WebSocketSessionHandlerDecorator(messageHandler, sessionManager,
                properties.getSendTimeLimit(), properties.getBufferSizeLimit());
    }

    @Bean
    @ConditionalOnMissingBean
    public WebSocketConfigurer naWebSocketConfigurer(WebSocketHandler webSocketHandler,
                                                     LoginUserHandshakeInterceptor handshakeInterceptor,
                                                     WebSocketProperties properties) {
        return registry -> registry
                .addHandler(webSocketHandler, properties.getPath())
                .addInterceptors(handshakeInterceptor)
                // 允许跨域，否则前端连接会被直接断开
                .setAllowedOriginPatterns(properties.getAllowedOrigins());
    }

    // ==================== 发送器 ====================

    /**
     * 本地发送器（单机）
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebSocketEnabled
    @ConditionalOnProperty(prefix = "na.websocket", name = "sender-type",
            havingValue = "local", matchIfMissing = true)
    public static class LocalSenderConfiguration {

        @Bean
        @ConditionalOnMissingBean(WebSocketMessageSender.class)
        public WebSocketMessageSender webSocketMessageSender(WebSocketSessionManager sessionManager,
                                                             ObjectMapper objectMapper) {
            return new LocalWebSocketMessageSender(sessionManager, objectMapper);
        }
    }

    /**
     * Redis 发送器（多实例广播）。
     *
     * <p>复用 na-tool 已有的 {@link RedisMessageListenerContainer}，不重复定义容器；
     * 容器或 StringRedisTemplate 缺失时降级为本地发送器。</p>
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebSocketEnabled
    @ConditionalOnProperty(prefix = "na.websocket", name = "sender-type", havingValue = "redis")
    public static class RedisSenderConfiguration {

        @Bean
        @ConditionalOnMissingBean(WebSocketMessageSender.class)
        public WebSocketMessageSender webSocketMessageSender(
                WebSocketSessionManager sessionManager,
                ObjectMapper objectMapper,
                WebSocketProperties properties,
                ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                ObjectProvider<RedisMessageListenerContainer> containerProvider) {
            LocalWebSocketMessageSender localSender = new LocalWebSocketMessageSender(sessionManager, objectMapper);
            StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
            RedisMessageListenerContainer container = containerProvider.getIfAvailable();
            if (redisTemplate == null || container == null) {
                log.warn("[WebSocket] sender-type=redis 但 Redis 容器或 StringRedisTemplate 缺失，已降级为 local。"
                        + "请检查 spring.data.redis.host 以及 na.redisListener.enable / na.redisMessage.enable");
                return localSender;
            }
            container.addMessageListener(
                    new RedisWebSocketMessageListener(localSender, objectMapper),
                    new ChannelTopic(properties.getRedisChannel()));
            log.info("[WebSocket] Redis 广播监听已注册, channel={}", properties.getRedisChannel());
            return new RedisWebSocketMessageSender(redisTemplate, objectMapper, properties.getRedisChannel());
        }
    }

    // ==================== 鉴权路径放行 ====================

    /**
     * 把 WebSocket 路径追加到 na.auth.skipApis，避免握手被 JwtAuthenticationTokenFilter 拦截。
     */
    @Bean
    public SmartInitializingSingleton naWebSocketSkipApisRegistrar(
            ObjectProvider<AutoAuthConfig> autoAuthConfigProvider,
            WebSocketProperties properties) {
        return () -> appendSkipApis(autoAuthConfigProvider.getIfAvailable(), properties.getPath());
    }

    private static void appendSkipApis(AutoAuthConfig autoAuthConfig, String path) {
        if (autoAuthConfig == null || !StringUtils.hasText(path)) {
            return;
        }
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        List<String> patterns = new ArrayList<>();
        patterns.add(normalizedPath);
        patterns.add(normalizedPath + "/**");

        List<String> skipApis = autoAuthConfig.getSkipApis();
        if (skipApis == null) {
            skipApis = new ArrayList<>();
            autoAuthConfig.setSkipApis(skipApis);
        }
        try {
            for (String pattern : patterns) {
                if (!skipApis.contains(pattern)) {
                    skipApis.add(pattern);
                }
            }
        } catch (UnsupportedOperationException e) {
            // 配置绑定可能返回不可变集合，整体替换
            List<String> copy = new ArrayList<>(skipApis);
            for (String pattern : patterns) {
                if (!copy.contains(pattern)) {
                    copy.add(pattern);
                }
            }
            autoAuthConfig.setSkipApis(copy);
        }
    }
}
