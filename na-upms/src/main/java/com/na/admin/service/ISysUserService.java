package com.na.admin.service;

import com.na.admin.controller.vo.req.ReqLoginVO;
import com.na.admin.controller.vo.req.sysuser.ReqSysUserPagingVO;
import com.na.admin.controller.vo.req.sysuser.ReqSysUserSaveVO;
import com.na.admin.controller.vo.req.sysuser.ReqSysUserUpdateVO;
import com.na.admin.controller.vo.req.sysuser.ReqVerifyPasswordVO;
import com.na.admin.controller.vo.res.sysuser.ReqBindDeptVO;
import com.na.admin.controller.vo.res.sysuser.ReqBindPostVO;
import com.na.admin.controller.vo.res.sysuser.ReqBindRoleVO;
import com.na.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;

/**
 * sys_user Service接口
 */
public interface ISysUserService {

    Result saveObj(ReqSysUserSaveVO vo);

    Result updateObj(ReqSysUserUpdateVO vo);

    Result deleteObj(Long id);

    Result queryPaging(ReqSysUserPagingVO pagingVO);

    Result login(ReqLoginVO vo, HttpServletRequest request);

    Result loginOut();

    /** 锁屏解锁：校验当前登录用户的登录密码 */
    Result verifyPassword(ReqVerifyPasswordVO vo);

    Result getUserInfoByUserId(Long userId);

    Result getBindRoleByUserId(Long userId);

    Result bindRole(ReqBindRoleVO vo);

    /** 查询用户已绑定部门ID（无绑定返回 null） */
    Result getBindDeptByUserId(Long userId);

    /** 用户绑定部门（deptId 传空表示解除绑定） */
    Result bindDept(ReqBindDeptVO vo);

    /** 查询用户已绑定岗位ID集合 */
    Result getBindPostByUserId(Long userId);

    /** 用户绑定岗位（postIds 传空数组表示清空） */
    Result bindPost(ReqBindPostVO vo);

    Result clearToken(Long userId);
}
