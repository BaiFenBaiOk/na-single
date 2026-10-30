package com.na.admin.formatter.auth;

import com.na.common.dto.MyAuthDto;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.utils.TokenUtil;
import com.na.syslog.ILogFormatter;
import com.na.syslog.LogFormatterContext;

import java.util.Objects;

/**
 * 操作日志格式化器基类。
 *
 * 把每个 Formatter 都要做的两件事收拢到这里，子类只需声明「哪个模块 + 什么操作 + 做什么」：
 * 1. beforeContext —— 拼出「【操作人】动作描述」的日志内容；
 * 2. afterContext  —— 从统一响应壳 R / Result 中解出 data 作为日志结果
 *    （否则记录的是整个包装对象，日志里看不到实际业务数据）。
 *
 * 匿名接口取不到登录态，操作人退化为 {@link #anonymousName()}。
 */
public abstract class AbstractLogFormatter implements ILogFormatter {

    /** 动作描述，如「创建用户」 */
    protected abstract String action();

    /** 取不到登录态时的操作人占位 */
    protected String anonymousName() {
        return "匿名";
    }

    @Override
    public void beforeContext(LogFormatterContext context) {
        MyAuthDto authDto = TokenUtil.getUser();
        String username = authDto != null && authDto.getUsername() != null
                ? authDto.getUsername()
                : anonymousName();
        context.setContent("【" + username + "】" + action());
        Long userId = TokenUtil.userId();
        context.setUserId(Objects.isNull(userId) ? null : userId.toString());
    }

    @Override
    public void afterContext(LogFormatterContext context) {
        Object result = context.getResult();
        if (result == null) {
            return;
        }
        Object data = null;
        // 兼容两种返回包装
        if (result instanceof R) {
            data = ((R<?>) result).getData();
        } else if (result instanceof Result) {
            data = ((Result<?>) result).getData();
        }
        // 如果拿到data，写入上下文；否则保留原始result
        if (data != null) {
            context.setResult(data);
        }
    }
}
