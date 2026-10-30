package com.na.params;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import com.na.common.base.BasePagesParam;

/**
 * sys_map_role_user查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysMapRoleUserParams extends BasePagesParam {

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

}