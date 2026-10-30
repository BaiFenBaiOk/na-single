package com.na.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * sys_dept数据库实体
 */
@Data
@TableName("sys_dept")
public class SysDeptEntity {

    /**  */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 上级部门ID（0=顶级部门） */
    private Long parentId;

    /** 部门名称 */
    private String name;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**  */
    private Boolean isDel;

    /** 显示顺序 */
    private Integer orderNum;

    /** 备注 */
    private String remark;

}
