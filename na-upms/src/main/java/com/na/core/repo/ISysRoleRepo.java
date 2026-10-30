package com.na.core.repo;

import com.na.entity.SysRoleEntity;
import com.na.model.SysRoleModel;
import com.na.params.SysRoleParams;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sys_role Repo服务接口
 */
public interface ISysRoleRepo extends IService<SysRoleEntity> {

    /** 新增对象 */
    SysRoleModel saveObj(SysRoleModel model);

    /** 修改对象 */
    Boolean updateObj(SysRoleModel model);

    /** 批量新增对象 */
    Boolean saveBatchObj(List<SysRoleModel> modelList);

    /** 批量修改对象 */
    Boolean updateBatch(List<SysRoleModel> modelList);

    /** 分页条件查询 */
    IPage<SysRoleModel> queryPaging(SysRoleParams params);

    /** 根据用户id查询角色 */
    List<SysRoleModel> queryRolesByUserId(Long userId);

    /** 根据角色对象查询角色 */
    SysRoleModel queryRoleByRoleObj(SysRoleModel sysRoleModel);

    /** 根据角色id查询角色 */
    SysRoleModel queryRoleById(Long id);

    /** 根据角色id集合查询角色 */
    List<SysRoleModel> queryRoleByRoleIds(Collection<? extends Serializable> roleIds);
}