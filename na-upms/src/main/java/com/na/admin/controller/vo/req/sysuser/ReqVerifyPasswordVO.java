package com.na.admin.controller.vo.req.sysuser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 锁屏解锁：校验当前登录用户的登录密码。
 */
@Data
@Schema(description = "校验登录密码")
public class ReqVerifyPasswordVO {

    @NotBlank(message = "密码不可为空")
    @Schema(description = "登录密码")
    private String passWord;
}
