package com.na.admin.controller.vo.req.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "SysDept新增入参")
@Data
public class ReqSysDeptSaveVO {

    /** 上级部门ID（0=顶级部门） */
    @Schema(description = "上级部门ID（0=顶级部门）")
    private Long parentId;

    /** 部门名称 */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 20, message = "部门名称长度不能超过20位")
    @Schema(description = "部门名称")
    private String name;

    /** 显示顺序 */
    @Schema(description = "显示顺序")
    private Integer orderNum;

    /** 备注 */
    @Size(max = 200, message = "备注长度不能超过200位")
    @Schema(description = "备注")
    private String remark;

}
