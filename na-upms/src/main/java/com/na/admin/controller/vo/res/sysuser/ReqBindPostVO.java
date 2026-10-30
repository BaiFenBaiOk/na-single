package com.na.admin.controller.vo.res.sysuser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 用户绑定岗位入参（多岗位；postIds 传空数组表示清空）
 */
@Data
public class ReqBindPostVO {

    @NotNull(message = "用户id不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "岗位ID集合（空数组表示清空全部岗位）")
    private List<Long> postIds;

}
