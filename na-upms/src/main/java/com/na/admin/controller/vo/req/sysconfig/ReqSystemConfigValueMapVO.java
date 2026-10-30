package com.na.admin.controller.vo.req.sysconfig;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReqSystemConfigValueMapVO {
    @NotEmpty(message = "code不能为空")
    @NotNull(message = "code不能为空")
    private List<String> codes;

}
