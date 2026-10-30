package com.na.admin.formatter.auth.role;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 创建角色 */
@Component
public class LogRoleSaveFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.syslog.enums.ModuleEnum.ROLE_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.ADD;
    }

    @Override
    protected String action() {
        return "创建角色";
    }
}
