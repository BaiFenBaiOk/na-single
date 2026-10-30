package com.na.admin.controller.vo.res.sysuser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户绑定部门入参（单个部门；deptId 传空表示解除绑定）
 */
@Data
public class ReqBindDeptVO {

    @NotNull(message = "用户id不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "部门ID（传空表示解除绑定）")
    private Long deptId;

}
