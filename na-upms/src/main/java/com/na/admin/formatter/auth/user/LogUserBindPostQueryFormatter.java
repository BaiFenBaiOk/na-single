package com.na.admin.formatter.auth.user;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 查询用户已绑定岗位 */
@Component
public class LogUserBindPostQueryFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return ModuleEnum.USER_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.QUERY;
    }

    @Override
    protected String action() {
        return "查询用户已绑定岗位";
    }
}
