package com.na.admin.formatter.auth.role;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 删除角色 */
@Component
public class LogRoleDeleteFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return ModuleEnum.ROLE_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.DELETE;
    }

    @Override
    protected String action() {
        return "删除角色";
    }
}
