package com.na.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * sys_map_user_post业务模型
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysMapUserPostModel {

    /** 用户ID */
    private Long userId;

    /** 岗位ID */
    private Long postId;

    /**
     * 非数据库单表字段（关联查询时回填）
     */
    private String postName;
}
