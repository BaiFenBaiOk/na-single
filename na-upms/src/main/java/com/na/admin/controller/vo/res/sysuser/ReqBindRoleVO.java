package com.na.admin.controller.vo.res.sysuser;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * @author pg
 * @date 2025年11月14日
 */
@Data
public class ReqBindRoleVO {
    @NotNull(message = "用户id不能为空")
    private Long userId;
//    @NotEmpty(message = "角色id不能为空")
//    @Valid
    private List<Long> roleIds;
}
