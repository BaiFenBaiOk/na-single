package com.na.admin.controller.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author : pg
 * date : 2023/11/2 12:56
 * @version : v1.0
 * description :
 */
@Schema
@Data
public class ReqLoginVO {
    @NotBlank(message = "用户名不可为空")
    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户密码")
    @NotBlank(message = "密码不可为空")
    private String passWord;

    @Schema(description = "验证码标识")
    @NotBlank(message = "验证码标识不可为空")
    private String uuid;

    @Schema(description = "验证码")
    @NotBlank(message = "验证码")
    private String captcha;

    public String getUserName() {
        return userName.trim().toLowerCase();
    }

    public String getPassWord() {
        return passWord.trim();
    }

}
