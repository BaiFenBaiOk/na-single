package com.na.admin.formatter.auth.config;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.admin.enmus.MyModelEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 查询系统配置值集合 */
@Component
public class LogConfigValueMapFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.admin.enmus.MyModelEnum.SYSTEM_CONFIG;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.QUERY;
    }

    @Override
    protected String action() {
        return "查询系统配置值集合";
    }
}
