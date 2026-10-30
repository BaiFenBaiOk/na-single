package com.na.admin.service;

import com.na.admin.controller.vo.req.sysmenu.ReqSysMenuPagingVO;
import com.na.admin.controller.vo.req.sysmenu.ReqSysMenuSaveVO;
import com.na.admin.controller.vo.req.sysmenu.ReqSysMenuUpdateVO;
import com.na.admin.controller.vo.res.sysmenu.ReqBindRoleMenuVO;
import com.na.admin.controller.vo.res.sysmenu.ReqRoleMenuVO;
import com.na.common.result.Result;

/**
 * sys_menu Service接口
 */
public interface ISysMenuService {

    Result saveObj(ReqSysMenuSaveVO vo);

    Result updateObj(ReqSysMenuUpdateVO vo);

    Result deleteObj(Long id);

    Result queryPaging(ReqSysMenuPagingVO pagingVO);

    Result tree();

    Result getMenuBtnByParentId(Long parentId);

    Result roleMenuPaging(ReqRoleMenuVO pagingVO);

    Result saveRoleMenu(ReqBindRoleMenuVO vo);
}
