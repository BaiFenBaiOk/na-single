package com.na.admin.controller;

import com.na.admin.controller.vo.req.ReqLoginVO;
import com.na.admin.formatter.auth.login.LogLoginFormatter;
import com.na.admin.formatter.auth.login.LogLoginOutFormatter;
import com.na.admin.service.ISysUserService;
import com.na.auth.annotation.AnonymousAccess;
import com.na.common.annotation.NoRepeatSubmitIP;
import com.na.common.exceptions.ApiException;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.syslog.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登陆模块")
@RestController
// 模块前缀 na-upms：与网关 / Nginx 的 /api/na-upms/** 分流规则保持一致
@RequestMapping("/api/na-upms/login")
@RequiredArgsConstructor
public class LoginController {
    private final ISysUserService sysUserService;

    @AnonymousAccess
    @Operation(summary = "登录")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.SERVER_ERROR)
    @OperationLog(formatter = LogLoginFormatter.class)
    @PostMapping
    @NoRepeatSubmitIP(value = 2000)
    public Result login(@RequestBody @Validated ReqLoginVO vo, HttpServletRequest request) {
        return sysUserService.login(vo, request);
    }

    @Operation(summary = "登出")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.SERVER_ERROR)
    @OperationLog(formatter = LogLoginOutFormatter.class)
    @GetMapping("/loginOut")
    public Result loginOut() {
        return sysUserService.loginOut();
    }
}
