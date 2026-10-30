package com.na.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * sys_post数据库实体
 */
@Data
@TableName("sys_post")
public class SysPostEntity {

    /**  */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 上级岗位ID（0=顶级；岗位为平铺结构，恒为 0） */
    private Long parentId;

    /** 岗位名称 */
    private String name;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**  */
    private Boolean isDel;

    /** 岗位编码（全局唯一） */
    private String code;

}
