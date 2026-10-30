package com.na.admin.formatter.auth.post;

import com.na.admin.formatter.auth.AbstractLogFormatter;
import com.na.admin.enmus.MyModelEnum;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

/** 查询岗位列表 */
@Component
public class LogPostPagingFormatter extends AbstractLogFormatter {

    @Override
    public IModelProvider module() {
        return MyModelEnum.POST_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.QUERY;
    }

    @Override
    protected String action() {
        return "查询岗位列表";
    }
}
