package com.na.common.login;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 登录态配置。
 *
 * <p>配置前缀：{@code na.login}</p>
 *
 * @author pg
 */
@Data
@ConfigurationProperties(prefix = "na.login")
public class LoginProperties {

    /**
     * 是否开启单点登录（Single Sign-On）。
     *
     * <ul>
     *     <li>{@code true}：同一账号只保留最新一次登录，旧 token 立即失效；</li>
     *     <li>{@code false}：允许多端 / 多标签页同时在线（默认）。</li>
     * </ul>
     *
     * 对应 Redis 会话 key：
     * <ul>
     *     <li>单点：{@code token:login:{userId}}</li>
     *     <li>多点：{@code token:login:{userId}:{token}}</li>
     * </ul>
     */
    private boolean singleToken = false;
}
