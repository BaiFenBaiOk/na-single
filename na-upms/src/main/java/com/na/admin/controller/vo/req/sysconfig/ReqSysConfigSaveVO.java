package com.na.admin.controller.vo.req.sysconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqSysConfigSaveVO {

    @Schema(description = "formId")
    @NotNull(message = "配置类型ID")
    private Long formId;

    @Schema(description = "name")
    @NotNull(message = "名称不能为空")
    private String name;

    @Schema(description = "code")
    @NotNull(message = "编码不能为空")
    private String code;

    @Schema(description = "value")
    @NotNull(message = "值不能为空")
    private String value;

    @Schema(description = "remarks")
    private String remarks;

    public @NotNull(message = "名称不能为空") String getName() {
        return name.trim();
    }

}
