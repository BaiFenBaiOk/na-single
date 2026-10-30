package com.na.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * sys_map_user_post数据库实体
 */
@Data
@TableName("sys_map_user_post")
public class SysMapUserPostEntity {

    /** 用户ID */
    private Long userId;

    /** 岗位ID */
    private Long postId;

    /** 岗位名称（关联查询回填，非表字段） */
    @TableField(exist = false)
    private String postName;

}
