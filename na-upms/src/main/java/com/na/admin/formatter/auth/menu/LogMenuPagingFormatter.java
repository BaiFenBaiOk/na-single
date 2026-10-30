package com.na.admin.formatter.auth.menu;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 分页查询菜单 */
@Component
public class LogMenuPagingFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return com.na.syslog.enums.ModuleEnum.MENU_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.QUERY;
    }

    @Override
    protected String action() {
        return "分页查询菜单";
    }
}
