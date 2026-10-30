package com.na.admin.controller;

import com.na.admin.formatter.auth.monitor.LogCacheFormatter;
import com.na.admin.formatter.auth.monitor.LogSysInfoFormatter;
import com.na.admin.utils.SysInfoUtil;
import com.na.auth.annotation.AnonymousAccess;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.utils.CacheTemplateUtil;
import com.na.syslog.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author pg
 * @date 2026年01月07日
 */
@Tag(name = "系统监控模块")
@RestController
// 模块前缀 na-upms：与网关 / Nginx 的 /api/na-upms/** 分流规则保持一致
@RequestMapping("/api/na-upms/systemInfo")
@RequiredArgsConstructor
public class SystemInfoController {

    private final CacheTemplateUtil cacheTemplateUtil;

    @Operation(summary = "缓存监控")
    @OperationLog(formatter = LogCacheFormatter.class)
    @GetMapping("/cache")
    @AnonymousAccess
    public Result cache() {
        Properties info = cacheTemplateUtil.execute((RedisCallback<Properties>) RedisServerCommands::info);
        Properties commandStats = cacheTemplateUtil
                .execute((RedisCallback<Properties>) connection -> connection.serverCommands().info("commandstats"));
        Object dbSize = cacheTemplateUtil.execute((RedisCallback<Object>) RedisServerCommands::dbSize);

        if (commandStats == null) {
            return R.error("获取异常");
        }

        Map<String, Object> result = new HashMap<>(3);
        result.put("info", info);
        result.put("dbSize", dbSize);

        List<Map<String, String>> pieList = new ArrayList<>();
        commandStats.stringPropertyNames().forEach(key -> {
            Map<String, String> data = new HashMap<>(2);
            String property = commandStats.getProperty(key);
            data.put("name", StringUtils.removeStart(key, "cmdstat_"));
            data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
            pieList.add(data);
        });

        result.put("commandStats", pieList);
        return R.successNotMsg(result);
    }

    @Operation(summary = "系统监控")
    @OperationLog(formatter = LogSysInfoFormatter.class)
    @GetMapping("/sysInfo")
    @AnonymousAccess
    public Result sysInfo() {
        return R.successNotMsg(SysInfoUtil.collect());
    }


}
