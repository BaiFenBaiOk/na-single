package com.na.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * sys_role业务模型
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysRoleModel implements Serializable {

    /**  */
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 显示顺序 */
    private Integer roleSort;

    /** 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限） */
    private String dataScope;

    /** 角色状态（false正常 true停用，对应数据库 0/1） */
    private Boolean status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 角色类型（00 系统内置 / 03 普通） */
    private String roleType;

    /**  */
    private Boolean isDel;

}