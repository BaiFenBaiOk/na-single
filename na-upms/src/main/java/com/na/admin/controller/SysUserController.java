package com.na.admin.controller;

import com.na.admin.controller.vo.req.sysuser.ReqSysUserPagingVO;
import com.na.admin.controller.vo.req.sysuser.ReqSysUserSaveVO;
import com.na.admin.controller.vo.req.sysuser.ReqSysUserUpdateVO;
import com.na.admin.controller.vo.req.sysuser.ReqVerifyPasswordVO;
import com.na.admin.controller.vo.res.sysuser.ReqBindDeptVO;
import com.na.admin.controller.vo.res.sysuser.ReqBindPostVO;
import com.na.admin.controller.vo.res.sysuser.ReqBindRoleVO;
import com.na.admin.formatter.auth.user.LogMenuBtnFormatter;
import com.na.admin.formatter.auth.user.LogUserInfoFormatter;
import com.na.admin.formatter.auth.user.LogUserKickFormatter;
import com.na.admin.formatter.auth.user.LogUserMenuTreeFormatter;
import com.na.admin.formatter.auth.user.LogUserPagingFormatter;
import com.na.admin.formatter.auth.user.LogUserBindDeptFormatter;
import com.na.admin.formatter.auth.user.LogUserDeleteFormatter;
import com.na.admin.formatter.auth.user.LogUserBindDeptQueryFormatter;
import com.na.admin.formatter.auth.user.LogUserBindPostFormatter;
import com.na.admin.formatter.auth.user.LogUserBindPostQueryFormatter;
import com.na.admin.formatter.auth.user.LogUserRoleBindFormatter;
import com.na.admin.formatter.auth.user.LogUserRoleQueryFormatter;
import com.na.admin.formatter.auth.user.LogUserSaveFormatter;
import com.na.admin.formatter.auth.user.LogUserUpdateFormatter;
import com.na.admin.service.ISysMenuService;
import com.na.admin.service.ISysUserService;
import com.na.auth.annotation.AuthNotCheckedApi;
import com.na.common.annotation.NoRepeatSubmit;
import com.na.common.base.BaseController;
import com.na.common.dto.MyAuthDto;
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

@Tag(name = "用户")
@RestController
// 模块前缀 na-upms：与网关 / Nginx 的 /api/na-upms/** 分流规则保持一致
@RequestMapping("/api/na-upms/sysuser")
@RequiredArgsConstructor
public class SysUserController extends BaseController {

    private final ISysUserService sysUserService;
    private final ISysMenuService sysMenuService;

    @Operation(summary = "创建")
    @OperationLog(formatter = LogUserSaveFormatter.class)
    @PostMapping("/save")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result save(@RequestBody @Validated ReqSysUserSaveVO vo){
        return sysUserService.saveObj(vo);
    }

    @Operation(summary = "编辑")
    @OperationLog(formatter = LogUserUpdateFormatter.class)
    @PutMapping("/update")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result update(@RequestBody @Validated ReqSysUserUpdateVO vo){
        return sysUserService.updateObj(vo);
    }

    @Operation(summary = "删除，仍有角色绑定则拒绝")
    @OperationLog(formatter = LogUserDeleteFormatter.class)
    @DeleteMapping("/delete/{id}")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    @Validated
    public Result delete(@PathVariable("id") @NotNull(message = "id不能为空") Long id) {
        return sysUserService.deleteObj(id);
    }

    @Operation(summary = "列表")
    @OperationLog(formatter = LogUserPagingFormatter.class)
    @PostMapping("/list-paging")
    @AuthNotCheckedApi
    public Result queryPaging(@RequestBody ReqSysUserPagingVO pagingVO){
        if(Status.SUCCESS != checkPageParams(pagingVO.getCurrentPage(), pagingVO.getPageSize())){
            return R.error(Status.ERROR);
        }
        return sysUserService.queryPaging(pagingVO);
    }

    @Operation(summary = "清空某个用户的token")
    @OperationLog(formatter = LogUserKickFormatter.class)
    @GetMapping("/clear/{userId}")
    @Validated
    @AuthNotCheckedApi
    public Result clearToken(@PathVariable("userId") @NotNull(message = "userId不能为空") Long userId) {
        return sysUserService.clearToken(userId);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @Operation(summary = "用户信息")
    @OperationLog(formatter = LogUserInfoFormatter.class)
    @GetMapping("/userInfo")
    @AuthNotCheckedApi
    public Result userInfo() {
        return sysUserService.getUserInfoByUserId(TokenUtil.userId());
    }

    /**
     * 锁屏解锁：校验当前登录用户的登录密码。
     * 不加 @OperationLog，避免密码进入操作日志。
     */
    @Operation(summary = "校验当前用户登录密码（锁屏解锁）")
    @PostMapping("/verifyPassword")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 1000)
    public Result verifyPassword(@RequestBody @Validated ReqVerifyPasswordVO vo) {
        return sysUserService.verifyPassword(vo);
    }

    @Operation(description = "路由个人菜单树")
    @OperationLog(formatter = LogUserMenuTreeFormatter.class)
    @GetMapping("/tree")
    @AuthNotCheckedApi
    public Result tree() {
        return sysMenuService.tree();
    }

    @GetMapping("/getMenuBtn/{menuId}")
    @Operation(summary = "获取按钮")
    @OperationLog(formatter = LogMenuBtnFormatter.class)
    @AuthNotCheckedApi
    @Validated
    public Result getMenuBtn(@PathVariable @NotNull(message = "用户id不能为空") Long menuId) {
        return sysMenuService.getMenuBtnByParentId(menuId);
    }

    @Operation(summary = "查询用户已经绑定的角色")
    @OperationLog(formatter = LogUserRoleQueryFormatter.class)
    @GetMapping("/getBindRoleByUserId/{userId}")
    @AuthNotCheckedApi
    @Validated
    public Result getBindRoleByUserId(@PathVariable @NotNull(message = "用户id不能为空") Long userId) {
        return sysUserService.getBindRoleByUserId(userId);
    }

    @Operation(summary = "用户绑定角色")
    @OperationLog(formatter = LogUserRoleBindFormatter.class)
    @PutMapping("/bindRole")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result bindRole(@RequestBody @Validated ReqBindRoleVO vo) {
        return sysUserService.bindRole(vo);
    }

    @Operation(summary = "查询用户已绑定部门")
    @OperationLog(formatter = LogUserBindDeptQueryFormatter.class)
    @GetMapping("/getBindDept/{userId}")
    @AuthNotCheckedApi
    @Validated
    public Result getBindDept(@PathVariable("userId") @NotNull(message = "用户id不能为空") Long userId) {
        return sysUserService.getBindDeptByUserId(userId);
    }

    @Operation(summary = "用户绑定部门（deptId 传空表示解除绑定）")
    @OperationLog(formatter = LogUserBindDeptFormatter.class)
    @PutMapping("/bindDept")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result bindDept(@RequestBody @Validated ReqBindDeptVO vo) {
        return sysUserService.bindDept(vo);
    }

    @Operation(summary = "查询用户已绑定岗位")
    @OperationLog(formatter = LogUserBindPostQueryFormatter.class)
    @GetMapping("/getBindPost/{userId}")
    @AuthNotCheckedApi
    @Validated
    public Result getBindPost(@PathVariable("userId") @NotNull(message = "用户id不能为空") Long userId) {
        return sysUserService.getBindPostByUserId(userId);
    }

    @Operation(summary = "用户绑定岗位（postIds 传空数组表示清空）")
    @OperationLog(formatter = LogUserBindPostFormatter.class)
    @PutMapping("/bindPost")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result bindPost(@RequestBody @Validated ReqBindPostVO vo) {
        return sysUserService.bindPost(vo);
    }

}