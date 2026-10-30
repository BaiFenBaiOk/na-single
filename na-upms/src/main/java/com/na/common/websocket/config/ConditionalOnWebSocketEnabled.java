package com.na.common.websocket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket 总开关条件：{@code na.websocket.enable=true}（缺省视为开启）。
 *
 * <p>除了自动配置本身，WebSocketAutoConfiguration 里的静态内部配置类
 * 也需要这个条件。项目使用 {@code @ComponentScan("com.na")} 宽泛扫描，
 * 静态内部 @Configuration 可能被单独扫描到，从而绕过外层条件。</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(prefix = "na.websocket", name = "enable",
        havingValue = "true", matchIfMissing = true)
public @interface ConditionalOnWebSocketEnabled {
}
