package com.na.params;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import com.na.common.base.BasePagesParam;

/**
 * sys_map_role_menu查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysMapRoleMenuParams extends BasePagesParam {

    /**  */
    private Long roleId;

    /**  */
    private Long menuId;

}