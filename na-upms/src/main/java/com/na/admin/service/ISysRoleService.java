package com.na.admin.service;

import com.na.admin.controller.vo.req.sysrole.ReqSysRolePagingVO;
import com.na.admin.controller.vo.req.sysrole.ReqSysRoleSaveVO;
import com.na.admin.controller.vo.req.sysrole.ReqSysRoleUpdateVO;
import com.na.common.result.Result;

/**
 * sys_role Service接口
 */
public interface ISysRoleService {

    Result saveObj(ReqSysRoleSaveVO vo);

    Result updateObj(ReqSysRoleUpdateVO vo);

    Result deleteObj(Long id);

    Result queryPaging(ReqSysRolePagingVO pagingVO);
}
