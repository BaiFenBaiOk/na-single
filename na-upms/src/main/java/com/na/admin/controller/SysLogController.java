package com.na.admin.controller;

import com.na.admin.formatter.auth.log.LogPagingFormatter;
import com.na.admin.service.ISysLogService;
import com.na.auth.annotation.AuthNotCheckedApi;
import com.na.common.base.BaseController;
import com.na.common.exceptions.ApiException;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.syslog.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

/**
 * @author pg
 * @date 2025年11月06日
 */
@Tag(name = "日志模块")
@RestController
// 模块前缀 na-upms：与网关 / Nginx 的 /api/na-upms/** 分流规则保持一致
@RequestMapping("/api/na-upms/log")
@RequiredArgsConstructor
public class SysLogController extends BaseController {

    private final ISysLogService sysLogService;

    @Operation(summary = "分页查询日志")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(Status.SERVER_ERROR)
    @OperationLog(formatter = LogPagingFormatter.class)
    @PostMapping("/list-paging")
    @AuthNotCheckedApi
    public Result paging(@RequestBody com.na.admin.controller.vo.req.logs.ReqLogPagingVO pagingVO) throws IOException {
        if (Status.SUCCESS != checkPageParams(pagingVO.getCurrentPage(), pagingVO.getPageSize())) {
            return R.error(Status.ERROR);
        }
        return sysLogService.querySysLogPaging(pagingVO);
    }
}
