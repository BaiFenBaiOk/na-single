package com.na.admin.controller.vo.res.sysmenu;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReqBindRoleMenuVO {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
    /** 允许空集合：表示清空该角色的全部菜单授权 */
    @NotNull(message = "菜单ID不能为空")
    private List<Long> menuIds;

}
