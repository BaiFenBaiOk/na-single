package com.na.admin.formatter.auth.role;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 编辑角色 */
@Component
public class LogRoleUpdateFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.syslog.enums.ModuleEnum.ROLE_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.UPDATE;
    }

    @Override
    protected String action() {
        return "编辑角色";
    }
}
