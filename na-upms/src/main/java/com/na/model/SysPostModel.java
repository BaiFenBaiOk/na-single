package com.na.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * sys_post业务模型
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysPostModel {

    /**  */
    private Long id;

    /** 上级岗位ID（0=顶级；岗位为平铺结构，恒为 0） */
    private Long parentId;

    /** 岗位名称 */
    private String name;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**  */
    private Boolean isDel;

    /** 岗位编码（全局唯一） */
    private String code;

}
