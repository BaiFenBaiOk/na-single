package com.na.admin.formatter.auth.monitor;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.admin.enmus.MyModelEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 查询服务器监控信息 */
@Component
public class LogSysInfoFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.admin.enmus.MyModelEnum.SYSTEM_MONITOR;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.QUERY;
    }

    @Override
    protected String action() {
        return "查询服务器监控信息";
    }
}
