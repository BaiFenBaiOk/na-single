package com.na.core.repo.impl;

import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.na.common.base.BasePagesDomain;
import com.na.common.utils.ObjConversionUtil;
import com.na.core.constant.Consts;

import java.util.List;
import com.na.core.repo.ISysMapRoleMenuRepo;
import com.na.core.mapper.ISysMapRoleMenuMapper;
import com.na.entity.SysMapRoleMenuEntity;
import com.na.model.SysMapRoleMenuModel;
import com.na.params.SysMapRoleMenuParams;

/**
 * sys_map_role_menu Repo实现类
 */
@Repository
public class SysMapRoleMenuRepoImpl extends ServiceImpl<ISysMapRoleMenuMapper, SysMapRoleMenuEntity> implements ISysMapRoleMenuRepo {

    @Override
    public Boolean saveObj(SysMapRoleMenuModel model) {
        return super.save(ObjConversionUtil.copyProperties(model, SysMapRoleMenuEntity.class));
    }

    @Override
    public Boolean updateObj(SysMapRoleMenuModel model) {
        return super.updateById(ObjConversionUtil.copyProperties(model, SysMapRoleMenuEntity.class));
    }

    @Override
    public Boolean saveBatchObj(List<SysMapRoleMenuModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysMapRoleMenuModel> subList = modelList.subList(i, end);
            List<SysMapRoleMenuEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysMapRoleMenuEntity.class);
            saveBatch(entities);
        }
        return true;
    }

    @Override
    public Boolean updateBatch(List<SysMapRoleMenuModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysMapRoleMenuModel> subList = modelList.subList(i, end);
            List<SysMapRoleMenuEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysMapRoleMenuEntity.class);
            updateBatchById(entities);
        }
        return true;
    }

    @Override
    public IPage<SysMapRoleMenuModel> queryPaging(SysMapRoleMenuParams params) {
        Page<SysMapRoleMenuEntity> page = new Page<>(params.getCurrentPage(), params.getPageSize());
        IPage<SysMapRoleMenuEntity> iPage = baseMapper.queryPaging(page, params);
        return BasePagesDomain.iPages(iPage, ObjConversionUtil.copyPropertiesList(iPage.getRecords(), SysMapRoleMenuModel.class));
    }

    @Override
    public List<SysMapRoleMenuModel> queryAllRoleMenuByRoleId(Long roleId) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryAllRoleMenuByRoleId(roleId), SysMapRoleMenuModel.class);
    }

    @Override
    public Boolean deleteMapRoleMenuByRoleId(Long roleId) {
        return baseMapper.deleteMapRoleMenuByRoleId(roleId) > 0;
    }

    @Override
    public Boolean deleteMapRoleMenuByMenuId(Long menuId) {
        return lambdaUpdate().eq(SysMapRoleMenuEntity::getMenuId, menuId).remove();
    }

    @Override
    public Boolean saveBatchMapRoleMenu(List<SysMapRoleMenuModel> list) {
        return saveBatch(ObjConversionUtil.copyPropertiesList(list, SysMapRoleMenuEntity.class));
    }

}