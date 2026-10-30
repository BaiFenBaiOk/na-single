package com.na.admin.formatter.auth.menu;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 角色菜单授权 */
@Component
public class LogRoleMenuBindFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.syslog.enums.ModuleEnum.MENU_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.AUTH;
    }

    @Override
    protected String action() {
        return "角色菜单授权";
    }
}
