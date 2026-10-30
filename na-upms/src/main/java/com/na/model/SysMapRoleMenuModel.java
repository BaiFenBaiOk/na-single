package com.na.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * sys_map_role_menu业务模型
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysMapRoleMenuModel {

    /**  */
    private Long roleId;

    /**  */
    private Long menuId;

}