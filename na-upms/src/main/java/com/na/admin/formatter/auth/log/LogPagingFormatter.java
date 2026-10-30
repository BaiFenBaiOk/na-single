package com.na.admin.formatter.auth.log;

import com.na.common.dto.MyAuthDto;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.utils.TokenUtil;
import com.na.syslog.ILogFormatter;
import com.na.syslog.LogFormatterContext;
import com.na.syslog.enums.IModelProvider;
import com.na.syslog.enums.IOperateProvider;
import com.na.syslog.enums.ModuleEnum;
import com.na.syslog.enums.OperateEnum;
import org.springframework.stereotype.Component;

@Component
public class LogPagingFormatter implements ILogFormatter {
    @Override
    public void beforeContext(LogFormatterContext context) {
        MyAuthDto authDto = TokenUtil.getUser();
        if(authDto != null) {
            String content = "【" + authDto.getUsername() + "】分页查询日志";
            context.setContent(content);
        }
    }

    @Override
    public void afterContext(LogFormatterContext context) {
        Object result = context.getResult();
        if (result == null) {
            return;
        }
        Object data = null;
        // 兼容两种返回包装
        if (result instanceof R) {
            R<?> resultObj = (R<?>) result;
            data = resultObj.getData();
        } else if (result instanceof Result) {
            Result<?> resultObj = (Result<?>) result;
            data = resultObj.getData();
        }
        // 如果拿到data，写入上下文；否则保留原始result
        if (data != null) {
            context.setResult(data);
        }
    }

    @Override
    public IModelProvider module() {
        return ModuleEnum.LOG_MANAGEMENT;
    }

    @Override
    public IOperateProvider operate() {
        return OperateEnum.QUERY;
    }
}
