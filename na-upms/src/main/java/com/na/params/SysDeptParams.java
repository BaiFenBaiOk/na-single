package com.na.params;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import com.na.common.base.BasePagesParam;

/**
 * sys_dept查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysDeptParams extends BasePagesParam {

    /**  */
    private Long id;

    /** 上级部门ID */
    private Long parentId;

    /** 部门名称 */
    private String name;

    /**  */
    private Boolean isDel;

}
