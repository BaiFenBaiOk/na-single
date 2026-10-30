package com.na.admin.formatter.auth.dept;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.admin.enmus.MyModelEnum;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 创建部门 */
@Component
public class LogDeptSaveFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return MyModelEnum.DEPT_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.ADD;
    }

    @Override
    protected String action() {
        return "创建部门";
    }
}
