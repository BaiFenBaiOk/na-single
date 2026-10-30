package com.na.admin.formatter.auth.post;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.admin.enmus.MyModelEnum;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 创建岗位 */
@Component
public class LogPostSaveFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return MyModelEnum.POST_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.ADD;
    }

    @Override
    protected String action() {
        return "创建岗位";
    }
}
