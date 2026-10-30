package com.na.admin.enmus;

import com.na.syslog.enums.IOperateProvider;

public enum MyOperateEnum implements IOperateProvider {

    ;

    private final String code;
    private final String desc;

    private MyOperateEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
