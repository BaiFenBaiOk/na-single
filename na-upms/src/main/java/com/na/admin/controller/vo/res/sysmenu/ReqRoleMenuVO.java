package com.na.admin.controller.vo.res.sysmenu;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author pg
 * @date 2025年11月14日
 */
@Data
public class ReqRoleMenuVO {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
