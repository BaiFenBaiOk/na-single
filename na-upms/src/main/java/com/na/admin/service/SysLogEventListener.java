package com.na.admin.service;

import com.alibaba.fastjson2.JSONObject;
import com.na.auth.AutoAuthConfig;
import com.na.common.utils.IDUtil;
import com.na.core.mapper.ISysLogMapper;
import com.na.entity.SysLogEntity;
import com.na.syslog.dto.SysLogDto;
import com.na.syslog.event.SysLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SysLogEventListener {

    @Autowired
    private ISysLogMapper sysLogMapper;
    @Autowired
    private AutoAuthConfig autoAuthConfig;

    /** text 列上限：utf8mb4 场景 65535 字节约等于 16383 个字符 */
    private static final int STR_MAX_LENGTH = 16383;
    /** 请求参数只保留前 1000 字符，避免大 body 撑爆日志表（业务取舍，非列宽限制） */
    private static final int PARAMS_MAX_LENGTH = 1000;
    /** 参数被截断时追加的提示后缀 */
    private static final String TRUNCATED_SUFFIX = "...(truncated)";

    @Async("sysLogThreadPool")
    @EventListener(SysLogEvent.class)
    public void handleSysLogEvent(SysLogEvent event) {
        if (event == null || event.getSource() == null) {
            return;
        }
        SysLogDto sysLogDto;
        try {
            sysLogDto = (SysLogDto) event.getSource();
        } catch (ClassCastException e) {
            // 如果类型不匹配，直接不处理
            return;
        }

        SysLogEntity entity = new SysLogEntity();
        entity.setId(sysLogDto.getId() == null ? IDUtil.ID(17) : sysLogDto.getId());
        entity.setRequestIp(StringUtils.defaultString(sysLogDto.getRequestIp()));
        entity.setType(StringUtils.defaultString(sysLogDto.getType()));
        entity.setDescription(StringUtils.defaultString(sysLogDto.getDescription()));
        entity.setClassPath(StringUtils.defaultString(sysLogDto.getClassPath()));
        entity.setActionMethod(StringUtils.defaultString(sysLogDto.getActionMethod()));
        entity.setRequestUri(truncate(sysLogDto.getRequestUri(), STR_MAX_LENGTH));
        entity.setHttpMethod(StringUtils.defaultString(sysLogDto.getHttpMethod()));
        entity.setParams(toParamsJson(sysLogDto.getParams()));
        entity.setResult(truncate(sysLogDto.getResult(), STR_MAX_LENGTH));
        entity.setExDesc(StringUtils.defaultString(sysLogDto.getExDesc()));
        entity.setExDetail(truncate(sysLogDto.getExDetail(), STR_MAX_LENGTH));
        entity.setStartTime(sysLogDto.getStartTime());
        entity.setFinishTime(sysLogDto.getFinishTime());
        entity.setConsumingTime(sysLogDto.getConsumingTime());
        entity.setModule(StringUtils.defaultString(sysLogDto.getModule()));
        entity.setOperate(StringUtils.defaultString(sysLogDto.getOperate()));
        entity.setContent(StringUtils.defaultString(sysLogDto.getContent()));
        entity.setUa(StringUtils.defaultString(sysLogDto.getUa()));
        entity.setOperateTime(sysLogDto.getOperateTime());
        entity.setUserId(toLong(sysLogDto.getUserId(), 0L));
        sysLogMapper.insert(entity);
    }

    /**
     * 序列化请求参数并截断。
     * 必须先判 null 再序列化：fastjson2 对 null 返回的是字符串 "null" 而不是 null，
     * 直接序列化会把字面量 "null" 写进库。
     */
    private static String toParamsJson(Object params) {
        if (params == null) {
            return "";
        }
        String json = JSONObject.toJSONString(params);
        if (json.length() <= PARAMS_MAX_LENGTH) {
            return json;
        }
        return truncate(json, PARAMS_MAX_LENGTH) + TRUNCATED_SUFFIX;
    }

    /**
     * 按 UTF-16 长度截断，空值返回空串。
     * 截断点若落在代理对中间（emoji、生僻字），会退一格，避免产生 MySQL utf8mb4
     * 无法编码的孤立代理字符（会导致整条日志写入失败或变成乱码）。
     */
    private static String truncate(String value, int maxLength) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        int end = maxLength;
        if (Character.isHighSurrogate(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(0, end);
    }

    private static Long toLong(String value, Long defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            log.warn("日志字段非数字，已置空: {}", value);
            return defaultValue;
        }
    }

}
