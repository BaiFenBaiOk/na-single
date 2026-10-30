package com.na.params;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import com.na.common.base.BasePagesParam;

/**
 * sys_post查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysPostParams extends BasePagesParam {

    /**  */
    private Long id;

    /** 岗位名称 */
    private String name;

    /** 岗位编码 */
    private String code;

    /**  */
    private Boolean isDel;

}
