package com.na.core.repo;

import com.na.entity.SysMapRoleMenuEntity;
import com.na.model.SysMapRoleMenuModel;
import com.na.params.SysMapRoleMenuParams;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sys_map_role_menu Repo服务接口
 */
public interface ISysMapRoleMenuRepo extends IService<SysMapRoleMenuEntity> {

    /** 新增对象 */
    Boolean saveObj(SysMapRoleMenuModel model);

    /** 修改对象 */
    Boolean updateObj(SysMapRoleMenuModel model);

    /** 批量新增对象 */
    Boolean saveBatchObj(List<SysMapRoleMenuModel> modelList);

    /** 批量修改对象 */
    Boolean updateBatch(List<SysMapRoleMenuModel> modelList);

    /** 分页条件查询 */
    IPage<SysMapRoleMenuModel> queryPaging(SysMapRoleMenuParams params);

    /** 查询指定角色已绑定的菜单 */
    List<SysMapRoleMenuModel> queryAllRoleMenuByRoleId(Long roleId);

    /** 删除指定角色的全部菜单绑定 */
    Boolean deleteMapRoleMenuByRoleId(Long roleId);

    /** 根据菜单ID删除所有角色的菜单绑定 */
    Boolean deleteMapRoleMenuByMenuId(Long menuId);

    /** 批量新增角色菜单 */
    Boolean saveBatchMapRoleMenu(List<SysMapRoleMenuModel> list);
}
