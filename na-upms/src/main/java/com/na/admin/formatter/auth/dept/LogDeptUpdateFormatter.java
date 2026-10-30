package com.na.admin.formatter.auth.dept;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.admin.enmus.MyModelEnum;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 编辑部门 */
@Component
public class LogDeptUpdateFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return MyModelEnum.DEPT_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.UPDATE;
    }

    @Override
    protected String action() {
        return "编辑部门";
    }
}
