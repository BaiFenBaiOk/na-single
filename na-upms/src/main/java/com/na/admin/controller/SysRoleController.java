package com.na.admin.controller;

import com.na.admin.controller.vo.req.sysrole.ReqSysRolePagingVO;
import com.na.admin.controller.vo.req.sysrole.ReqSysRoleSaveVO;
import com.na.admin.controller.vo.req.sysrole.ReqSysRoleUpdateVO;
import com.na.admin.formatter.auth.role.LogRoleDeleteFormatter;
import com.na.admin.formatter.auth.role.LogRolePagingFormatter;
import com.na.admin.formatter.auth.role.LogRoleSaveFormatter;
import com.na.admin.formatter.auth.role.LogRoleUpdateFormatter;
import com.na.admin.service.ISysRoleService;
import com.na.auth.annotation.AuthNotCheckedApi;
import com.na.common.annotation.NoRepeatSubmit;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.base.BaseController;
import com.na.common.result.enums.Status;
import com.na.syslog.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "角色")
@RestController
// 模块前缀 na-upms：与网关 / Nginx 的 /api/na-upms/** 分流规则保持一致
@RequestMapping("/api/na-upms/sysrole")
@RequiredArgsConstructor
public class SysRoleController extends BaseController {

    private final ISysRoleService sysRoleService;

    @Operation(summary ="创建")
    @OperationLog(formatter = LogRoleSaveFormatter.class)
    @PostMapping("/save")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result save(@RequestBody @Validated ReqSysRoleSaveVO vo){
        return sysRoleService.saveObj(vo);
    }

    @Operation(summary ="编辑")
    @OperationLog(formatter = LogRoleUpdateFormatter.class)
    @PutMapping("/update")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result update(@RequestBody @Validated ReqSysRoleUpdateVO vo){
        return sysRoleService.updateObj(vo);
    }

    @Operation(summary = "删除，有用户绑定则拒绝")
    @OperationLog(formatter = LogRoleDeleteFormatter.class)
    @DeleteMapping("/delete/{id}")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    @Validated
    public Result delete(@PathVariable("id") @NotNull(message = "id不能为空") Long id) {
        return sysRoleService.deleteObj(id);
    }

    @Operation(summary ="列表")
    @OperationLog(formatter = LogRolePagingFormatter.class)
    @PostMapping("/list-paging")
    @AuthNotCheckedApi
    public Result queryPaging(@RequestBody ReqSysRolePagingVO pagingVO){
        if(Status.SUCCESS != checkPageParams(pagingVO.getCurrentPage(), pagingVO.getPageSize())){
            return R.error(Status.ERROR);
        }
        return sysRoleService.queryPaging(pagingVO);
    }

}