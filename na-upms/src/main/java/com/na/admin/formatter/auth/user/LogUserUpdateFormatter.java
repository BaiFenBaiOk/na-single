package com.na.admin.formatter.auth.user;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 编辑用户 */
@Component
public class LogUserUpdateFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.syslog.enums.ModuleEnum.USER_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.UPDATE;
    }

    @Override
    protected String action() {
        return "编辑用户";
    }
}
