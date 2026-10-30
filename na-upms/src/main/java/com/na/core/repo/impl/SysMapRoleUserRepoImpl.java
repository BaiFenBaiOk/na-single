package com.na.core.repo.impl;

import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.na.common.base.BasePagesDomain;
import com.na.common.utils.ObjConversionUtil;
import com.na.core.constant.Consts;
import java.util.List;
import com.na.core.repo.ISysMapRoleUserRepo;
import com.na.core.mapper.ISysMapRoleUserMapper;
import com.na.entity.SysMapRoleUserEntity;
import com.na.model.SysMapRoleUserModel;
import com.na.params.SysMapRoleUserParams;

/**
 * sys_map_role_user Repo实现类
 */
@Repository
public class SysMapRoleUserRepoImpl extends ServiceImpl<ISysMapRoleUserMapper, SysMapRoleUserEntity> implements ISysMapRoleUserRepo {

    @Override
    public Boolean saveObj(SysMapRoleUserModel model) {
        return super.save(ObjConversionUtil.copyProperties(model, SysMapRoleUserEntity.class));
    }

    @Override
    public Boolean updateObj(SysMapRoleUserModel model) {
        return super.updateById(ObjConversionUtil.copyProperties(model, SysMapRoleUserEntity.class));
    }

    @Override
    public Boolean saveBatchObj(List<SysMapRoleUserModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysMapRoleUserModel> subList = modelList.subList(i, end);
            List<SysMapRoleUserEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysMapRoleUserEntity.class);
            saveBatch(entities);
        }
        return true;
    }

    @Override
    public Boolean updateBatch(List<SysMapRoleUserModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysMapRoleUserModel> subList = modelList.subList(i, end);
            List<SysMapRoleUserEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysMapRoleUserEntity.class);
            updateBatchById(entities);
        }
        return true;
    }

    @Override
    public IPage<SysMapRoleUserModel> queryPaging(SysMapRoleUserParams params) {
        Page<SysMapRoleUserEntity> page = new Page<>(params.getCurrentPage(), params.getPageSize());
        IPage<SysMapRoleUserEntity> iPage = baseMapper.queryPaging(page, params);
        return BasePagesDomain.iPages(iPage, ObjConversionUtil.copyPropertiesList(iPage.getRecords(), SysMapRoleUserModel.class));
    }

    @Override
    public Boolean deleteRoleByUserId(Long userId) {
        return lambdaUpdate().eq(SysMapRoleUserEntity::getUserId, userId).remove();
    }

    @Override
    public Long countByRoleId(Long roleId) {
        return lambdaQuery().eq(SysMapRoleUserEntity::getRoleId, roleId).count();
    }

    @Override
    public Long countByUserId(Long userId) {
        return lambdaQuery().eq(SysMapRoleUserEntity::getUserId, userId).count();
    }

    @Override
    public Boolean saveRoleUserMap(List<SysMapRoleUserModel> models) {
        return saveBatch(ObjConversionUtil.copyPropertiesList(models, SysMapRoleUserEntity.class));
    }

}