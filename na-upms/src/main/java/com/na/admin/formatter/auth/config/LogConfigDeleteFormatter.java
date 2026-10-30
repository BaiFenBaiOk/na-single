package com.na.admin.formatter.auth.config;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.admin.enmus.MyModelEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 删除系统配置项 */
@Component
public class LogConfigDeleteFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.admin.enmus.MyModelEnum.SYSTEM_CONFIG;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.DELETE;
    }

    @Override
    protected String action() {
        return "删除系统配置项";
    }
}
