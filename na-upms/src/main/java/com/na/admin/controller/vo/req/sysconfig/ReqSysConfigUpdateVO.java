package com.na.admin.controller.vo.req.sysconfig;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author pg
 * @date 2025年12月15日
 */
@Data
public class ReqSysConfigUpdateVO {
    @NotNull(message = "id不能为空")
    private Long id;
    @NotNull(message = "value不能为空")
    private String value;
    @NotNull(message = "remark不能为空")
    private String remarks;
}
