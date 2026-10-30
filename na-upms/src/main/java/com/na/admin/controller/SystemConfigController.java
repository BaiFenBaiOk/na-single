package com.na.admin.controller;

import com.na.admin.controller.vo.req.sysconfig.*;
import com.na.admin.formatter.auth.config.LogConfigDeleteFormatter;
import com.na.admin.formatter.auth.config.LogConfigPagingFormatter;
import com.na.admin.formatter.auth.config.LogConfigSaveFormatter;
import com.na.admin.formatter.auth.config.LogConfigUpdateFormatter;
import com.na.admin.formatter.auth.config.LogConfigValueAllFormatter;
import com.na.admin.formatter.auth.config.LogConfigValueFormatter;
import com.na.admin.formatter.auth.config.LogConfigValueMapFormatter;
import com.na.admin.service.ISystemConfigService;
import com.na.auth.annotation.AnonymousAccess;
import com.na.auth.annotation.AuthNotCheckedApi;
import com.na.common.annotation.NoRepeatSubmit;
import com.na.common.base.BaseController;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.syslog.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "系统配置标签相关")
@RestController
// 模块前缀 na-upms：与网关 / Nginx 的 /api/na-upms/** 分流规则保持一致
@RequestMapping("/api/na-upms/sys-config")
public class SystemConfigController extends BaseController {
    @Autowired
    private ISystemConfigService systemConfigService;


    @Operation(description = "列表")
    @OperationLog(formatter = LogConfigPagingFormatter.class)
    @PostMapping("/list-paging")
    @AuthNotCheckedApi
    public Result paging(@RequestBody ReqSysConfigPagingVO pagingVO) throws IOException {
        if (Status.SUCCESS != checkPageParams(pagingVO.getCurrentPage(), pagingVO.getPageSize())) {
            return Result.error(Status.ERROR);
        }
        return systemConfigService.queryPaging(pagingVO);
    }

    @Operation(description = "编辑")
    @OperationLog(formatter = LogConfigUpdateFormatter.class)
    @PostMapping("/update")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result update(@RequestBody @Validated ReqSysConfigUpdateVO vo) throws IOException {
        return systemConfigService.update(vo);
    }

    @Operation(description = "创建")
    @OperationLog(formatter = LogConfigSaveFormatter.class)
    @PostMapping("/save")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result save(@RequestBody @Validated ReqSysConfigSaveVO vo) throws IOException {
        return systemConfigService.save(vo);
    }

    @Operation(description = "查询value")
    @AnonymousAccess
    @OperationLog(formatter = LogConfigValueFormatter.class)
    @PostMapping("/get-value")
    public Result queryValueByName(@RequestBody @Validated ReqSystemConfigValueVO vo) throws IOException {
        return systemConfigService.queryValueByName(vo);
    }

    @Operation(description = "查询value集合")
    @AnonymousAccess
    @OperationLog(formatter = LogConfigValueMapFormatter.class)
    @PostMapping("/get-value-map")
    public Result queryValueMapByName(@RequestBody @Validated ReqSystemConfigValueMapVO vo) throws IOException {
        return systemConfigService.queryValueMapByName(vo);
    }

    @Operation(description = "查询所有value")
    @AnonymousAccess
    @OperationLog(formatter = LogConfigValueAllFormatter.class)
    @GetMapping("/get-value-all")
    public Result queryValueAll() throws IOException {
        return systemConfigService.queryValueAll();
    }

    @Operation(description = "删除配置项")
    @OperationLog(formatter = LogConfigDeleteFormatter.class)
    @GetMapping("/delete/{id}")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    @Validated
    public Result deleteObj(@PathVariable @NotNull(message = "id不能为空") Long id) throws IOException {
        return systemConfigService.deleteObj(id);
    }

}
