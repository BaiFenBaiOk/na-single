package com.na.admin.controller;

import com.na.admin.controller.vo.req.dept.ReqSysDeptSaveVO;
import com.na.admin.controller.vo.req.dept.ReqSysDeptUpdateVO;
import com.na.admin.formatter.auth.dept.LogDeptDeleteFormatter;
import com.na.admin.formatter.auth.dept.LogDeptSaveFormatter;
import com.na.admin.formatter.auth.dept.LogDeptTreeFormatter;
import com.na.admin.formatter.auth.dept.LogDeptUpdateFormatter;
import com.na.admin.service.ISysDeptService;
import com.na.auth.annotation.AuthNotCheckedApi;
import com.na.common.annotation.NoRepeatSubmit;
import com.na.common.base.BaseController;
import com.na.common.result.Result;
import com.na.syslog.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "部门")
@RestController
// 模块前缀 na-upms：与网关 / Nginx 的 /api/na-upms/** 分流规则保持一致
@RequestMapping("/api/na-upms/dept")
@RequiredArgsConstructor
public class SysDeptController extends BaseController {

    private final ISysDeptService sysDeptService;

    @Operation(summary = "部门树")
    @OperationLog(formatter = LogDeptTreeFormatter.class)
    @GetMapping("/tree")
    @AuthNotCheckedApi
    public Result tree() {
        return sysDeptService.tree();
    }

    @Operation(summary = "创建")
    @OperationLog(formatter = LogDeptSaveFormatter.class)
    @PostMapping("/save")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result save(@RequestBody @Validated ReqSysDeptSaveVO vo) {
        return sysDeptService.saveObj(vo);
    }

    @Operation(summary = "编辑")
    @OperationLog(formatter = LogDeptUpdateFormatter.class)
    @PutMapping("/update")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result update(@RequestBody @Validated ReqSysDeptUpdateVO vo) {
        return sysDeptService.updateObj(vo);
    }

    @Operation(summary = "删除，有子部门或用户绑定则拒绝")
    @OperationLog(formatter = LogDeptDeleteFormatter.class)
    @DeleteMapping("/delete/{id}")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    @Validated
    public Result delete(@PathVariable("id") @NotNull(message = "id不能为空") Long id) {
        return sysDeptService.deleteObj(id);
    }

}
