package com.na.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * sys_map_role_user业务模型
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysMapRoleUserModel {

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

}