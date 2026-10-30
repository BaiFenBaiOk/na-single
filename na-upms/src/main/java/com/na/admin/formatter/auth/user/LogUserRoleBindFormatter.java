package com.na.admin.formatter.auth.user;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 用户绑定角色 */
@Component
public class LogUserRoleBindFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.syslog.enums.ModuleEnum.USER_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.BIND;
    }

    @Override
    protected String action() {
        return "用户绑定角色";
    }
}
