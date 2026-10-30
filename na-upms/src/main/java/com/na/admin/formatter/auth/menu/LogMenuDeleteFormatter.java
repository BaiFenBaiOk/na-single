package com.na.admin.formatter.auth.menu;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 删除菜单 */
@Component
public class LogMenuDeleteFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return ModuleEnum.MENU_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.DELETE;
    }

    @Override
    protected String action() {
        return "删除菜单";
    }
}
