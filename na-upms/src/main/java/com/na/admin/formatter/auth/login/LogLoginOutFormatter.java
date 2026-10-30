package com.na.admin.formatter.auth.login;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 登出 */
@Component
public class LogLoginOutFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.syslog.enums.ModuleEnum.LOGIN_OUT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.LOGOUT;
    }

    @Override
    protected String action() {
        return "登出";
    }
}
