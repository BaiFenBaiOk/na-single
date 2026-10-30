package com.na.core.repo;

import com.na.entity.SysMenuEntity;
import com.na.model.SysMenuModel;
import com.na.params.QueryMenuParams;
import com.na.params.SysMenuParams;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sys_menu Repo服务接口
 */
public interface ISysMenuRepo extends IService<SysMenuEntity> {

    /** 新增对象 */
    Boolean saveObj(SysMenuModel model);

    /** 修改对象 */
    Boolean updateObj(SysMenuModel model);

    /** 批量新增对象 */
    Boolean saveBatchObj(List<SysMenuModel> modelList);

    /** 批量修改对象 */
    Boolean updateBatch(List<SysMenuModel> modelList);

    /** 分页条件查询 */
    IPage<SysMenuModel> queryPaging(SysMenuParams params);

    List<SysMenuModel> queryMenuAll(QueryMenuParams params);

    List<SysMenuModel> selectMenuTreeByUserId(Long userId);

    List<SysMenuModel> queryMenuAll();

    List<SysMenuModel> queryMenuByUserId(Long userId);

    List<SysMenuModel> selectMenuTreeAll(Collection<? extends Serializable> types);

    List<SysMenuModel> getMenuBtnByParentId(Long parentId);

    List<SysMenuModel> getMenuBtnByParentIdAndUserId(Long parentId, Long userId);

    List<SysMenuModel> queryByIds(Collection<? extends Serializable> ids);
}