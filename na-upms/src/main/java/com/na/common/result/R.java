package com.na.common.result;

import com.na.common.result.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 通用响应结果（自包含实现，无外部依赖）
 *
 * @author na
 */
public class R<T> extends Result{

    @Schema(description = "响应状态码", example = "200")
    private Integer code;

    @Schema(description = "响应信息", example = "成功")
    private String msg;

    @Schema(description = "响应数据")
    private T data;

    public R() {
        super();
    }

    public static <T> Result<T> successNotMsg() {
        return new Result<>(Status.DEFAULT.getCode(), null, null);
    }
}
