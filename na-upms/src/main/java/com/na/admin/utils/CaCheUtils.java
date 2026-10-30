package com.na.admin.utils;

import com.na.common.constant.CacheSysConfigConst;
import org.springframework.stereotype.Component;

/**
 * 系统配置缓存 key 工具。
 *
 * <p>配置为全局配置，缓存 key 使用固定全局前缀。</p>
 *
 * @author pg
 * @date 2026年01月12日
 */
@Component
public class CaCheUtils {

    /**
     * 全局配置缓存 key
     */
    public String getCacheKey() {
        return String.format("%s:%s",
                CacheSysConfigConst.CONFIG_LIST_KEY, CacheSysConfigConst.CONFIG_LIST);
    }
}
