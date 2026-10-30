package com.na.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * sys_dept业务模型
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysDeptModel {

    /**  */
    private Long id;

    /** 上级部门ID（0=顶级部门） */
    private Long parentId;

    /** 部门名称 */
    private String name;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**  */
    private Boolean isDel;

    /** 显示顺序 */
    private Integer orderNum;

    /** 备注 */
    private String remark;

}
