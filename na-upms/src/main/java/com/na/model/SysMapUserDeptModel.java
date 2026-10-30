package com.na.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * sys_map_user_dept业务模型
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysMapUserDeptModel {

    /** 用户ID */
    private Long userId;

    /** 部门ID */
    private Long deptId;

    /**
     * 非数据库单表字段（关联查询时回填）
     */
    private String deptName;
}
