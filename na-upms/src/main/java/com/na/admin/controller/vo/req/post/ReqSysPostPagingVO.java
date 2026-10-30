package com.na.admin.controller.vo.req.post;

import com.na.common.base.BasePagesParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "SysPost分页查询入参")
@Data
public class ReqSysPostPagingVO extends BasePagesParam {

    /** 岗位名称 */
    @Schema(description = "岗位名称")
    private String name;

    /** 岗位编码 */
    @Schema(description = "岗位编码")
    private String code;

    /**  */
    @Schema(description = "")
    private Boolean isDel;

}
