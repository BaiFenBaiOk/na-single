package com.na.admin.enmus;

import com.na.syslog.enums.IModelProvider;

/**
 * 项目自有的日志模块提供者。
 *
 * na-syslog 的 {@link com.na.syslog.enums.ModuleEnum} 未覆盖的模块在此补充，
 * 取值风格与之一致：code 为大写下划线英文，desc 为中文说明。
 */
public enum MyModelEnum implements IModelProvider {

    /** 系统配置（/api/sys-config） */
    SYSTEM_CONFIG("SYSTEM_CONFIG", "系统配置"),

    /** 系统监控（/api/systemInfo） */
    SYSTEM_MONITOR("SYSTEM_MONITOR", "系统监控"),

    /** 部门管理（/api/dept） */
    DEPT_MANAGEMENT("DEPT_MANAGEMENT", "部门管理"),

    /** 岗位管理（/api/post） */
    POST_MANAGEMENT("POST_MANAGEMENT", "岗位管理"),
    ;

    private final String code;
    private final String desc;

    MyModelEnum(String code, String desc) {
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
