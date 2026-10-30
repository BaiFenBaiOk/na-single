package com.na.admin.controller.vo.req.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "SysPost新增入参")
@Data
public class ReqSysPostSaveVO {

    /** 岗位名称 */
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 20, message = "岗位名称长度不能超过20位")
    @Schema(description = "岗位名称")
    private String name;

    /** 岗位编码（全局唯一） */
    @NotBlank(message = "岗位编码不能为空")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{1,19}$", message = "岗位编码以英文字母开头，只能包含英文字符、数字和下划线，长度2-20位")
    @Schema(description = "岗位编码（全局唯一）")
    private String code;

}
