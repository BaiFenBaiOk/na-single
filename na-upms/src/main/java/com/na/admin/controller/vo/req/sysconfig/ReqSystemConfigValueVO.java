package com.na.admin.controller.vo.req.sysconfig;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqSystemConfigValueVO {
    @NotBlank(message = "code不能为空")
    private String code;

}