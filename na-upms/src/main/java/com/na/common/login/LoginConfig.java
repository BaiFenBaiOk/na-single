package com.na.common.login;

import com.na.common.utils.CacheTemplateUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 登录态配置。
 *
 * <p>被 {@code @ComponentScan("com.na")} 自动扫描，upms / flow 均会生效。</p>
 *
 * @author pg
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LoginProperties.class)
public class LoginConfig {

    @Bean
    @ConditionalOnMissingBean
    public LoginSessionService loginSessionService(CacheTemplateUtil cacheTemplateUtil,
                                                   LoginProperties loginProperties) {
        return new LoginSessionService(cacheTemplateUtil, loginProperties);
    }
}
