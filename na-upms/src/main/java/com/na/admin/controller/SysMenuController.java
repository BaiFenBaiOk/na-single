package com.na.admin.controller;

import com.na.admin.controller.vo.req.sysmenu.ReqSysMenuPagingVO;
import com.na.admin.controller.vo.req.sysmenu.ReqSysMenuSaveVO;
import com.na.admin.controller.vo.req.sysmenu.ReqSysMenuUpdateVO;
import com.na.admin.controller.vo.res.sysmenu.ReqBindRoleMenuVO;
import com.na.admin.controller.vo.res.sysmenu.ReqRoleMenuVO;
import com.na.admin.formatter.auth.menu.LogMenuDeleteFormatter;
import com.na.admin.formatter.auth.menu.LogMenuPagingFormatter;
import com.na.admin.formatter.auth.menu.LogMenuSaveFormatter;
import com.na.admin.formatter.auth.menu.LogMenuUpdateFormatter;
import com.na.admin.formatter.auth.menu.LogRoleMenuBindFormatter;
import com.na.admin.formatter.auth.menu.LogRoleMenuPagingFormatter;
import com.na.admin.service.ISysMenuService;
import com.na.auth.annotation.AuthNotCheckedApi;
import com.na.common.annotation.NoRepeatSubmit;
import com.na.common.base.BaseController;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.common.utils.TokenUtil;
import com.na.syslog.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "菜单及按钮")
@RestController
@RequestMapping("/api/na-upms/sysmenu")
@RequiredArgsConstructor
public class SysMenuController extends BaseController {

    private final ISysMenuService sysMenuService;

    @Operation(summary = "创建")
    @OperationLog(formatter = LogMenuSaveFormatter.class)
    @PostMapping("/save")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result save(@RequestBody @Validated ReqSysMenuSaveVO vo) {
        return sysMenuService.saveObj(vo);
    }

    @Operation(summary = "编辑")
    @OperationLog(formatter = LogMenuUpdateFormatter.class)
    @PutMapping("/update")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result update(@RequestBody @Validated ReqSysMenuUpdateVO vo) {
        return sysMenuService.updateObj(vo);
    }

    @Operation(summary = "删除，有子菜单则拒绝")
    @OperationLog(formatter = LogMenuDeleteFormatter.class)
    @DeleteMapping("/delete/{id}")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    @Validated
    public Result delete(@PathVariable("id") @NotNull(message = "id不能为空") Long id) {
        return sysMenuService.deleteObj(id);
    }

    @Operation(summary = "列表")
    @OperationLog(formatter = LogMenuPagingFormatter.class)
    @PostMapping("/list-paging")
    @AuthNotCheckedApi
    public Result queryPaging(@RequestBody ReqSysMenuPagingVO pagingVO) {
        if (Status.SUCCESS != checkPageParams(pagingVO.getCurrentPage(), pagingVO.getPageSize())) {
            return R.error(Status.ERROR);
        }
        if (!TokenUtil.isDefAuth()) {
            return R.error(Status.USER_NO_ACCESS);
        }
        return sysMenuService.queryPaging(pagingVO);
    }

    @Operation(summary = "角色分配菜单：返回全量菜单树和该角色已绑定的菜单")
    @OperationLog(formatter = LogRoleMenuPagingFormatter.class)
    @PostMapping("/list-paging-role-format")
    @AuthNotCheckedApi
    public Result roleMenuPaging(@RequestBody @Validated ReqRoleMenuVO pagingVO) {
        return sysMenuService.roleMenuPaging(pagingVO);
    }

    @Operation(summary = "角色绑定菜单")
    @OperationLog(formatter = LogRoleMenuBindFormatter.class)
    @PostMapping("/save-role-menu")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result saveRoleMenu(@RequestBody @Validated ReqBindRoleMenuVO vo) {
        return sysMenuService.saveRoleMenu(vo);
    }
}
