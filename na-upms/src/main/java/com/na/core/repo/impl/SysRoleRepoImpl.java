package com.na.core.repo.impl;

import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.na.common.base.BasePagesDomain;
import com.na.common.utils.ObjConversionUtil;
import com.na.core.constant.Consts;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import com.na.core.repo.ISysRoleRepo;
import com.na.core.mapper.ISysRoleMapper;
import com.na.entity.SysRoleEntity;
import com.na.model.SysRoleModel;
import com.na.params.SysRoleParams;

/**
 * sys_role Repo实现类
 */
@Repository
public class SysRoleRepoImpl extends ServiceImpl<ISysRoleMapper, SysRoleEntity> implements ISysRoleRepo {

    @Override
    public SysRoleModel saveObj(SysRoleModel model) {
        SysRoleEntity entity = ObjConversionUtil.copyProperties(model, SysRoleEntity.class);
        save(entity);
        return ObjConversionUtil.copyProperties(entity, SysRoleModel.class);
    }

    @Override
    public Boolean updateObj(SysRoleModel model) {
        return super.updateById(ObjConversionUtil.copyProperties(model, SysRoleEntity.class));
    }

    @Override
    public Boolean saveBatchObj(List<SysRoleModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysRoleModel> subList = modelList.subList(i, end);
            List<SysRoleEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysRoleEntity.class);
            saveBatch(entities);
        }
        return true;
    }

    @Override
    public Boolean updateBatch(List<SysRoleModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysRoleModel> subList = modelList.subList(i, end);
            List<SysRoleEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysRoleEntity.class);
            updateBatchById(entities);
        }
        return true;
    }

    @Override
    public IPage<SysRoleModel> queryPaging(SysRoleParams params) {
        Page<SysRoleEntity> page = new Page<>(params.getCurrentPage(), params.getPageSize());
        IPage<SysRoleEntity> iPage = baseMapper.queryPaging(page, params);
        return BasePagesDomain.iPages(iPage, ObjConversionUtil.copyPropertiesList(iPage.getRecords(), SysRoleModel.class));
    }

    @Override
    public List<SysRoleModel> queryRolesByUserId(Long userId) {
        List<SysRoleEntity> entities = baseMapper.queryRoleByUserId(userId);
        return ObjConversionUtil.copyPropertiesList(entities, SysRoleModel.class);
    }

    @Override
    public SysRoleModel queryRoleByRoleObj(SysRoleModel sysRoleModel) {
        return ObjConversionUtil.copyProperties(baseMapper.queryRoleByRoleObj(ObjConversionUtil.copyProperties(sysRoleModel, SysRoleEntity.class)), SysRoleModel.class);
    }

    @Override
    public SysRoleModel queryRoleById(Long id) {
        return ObjConversionUtil.copyProperties(getById(id), SysRoleModel.class);
    }

    @Override
    public List<SysRoleModel> queryRoleByRoleIds(Collection<? extends Serializable> roleIds) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryRoleByRoleIds(roleIds), SysRoleModel.class);
    }

}