package com.na.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * sys_map_user_dept数据库实体
 */
@Data
@TableName("sys_map_user_dept")
public class SysMapUserDeptEntity {

    /** 用户ID */
    private Long userId;

    /** 部门ID */
    private Long deptId;

    /** 部门名称（关联查询回填，非表字段） */
    @TableField(exist = false)
    private String deptName;

}
