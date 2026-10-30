package com.na.core.repo.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.na.common.base.BasePagesDomain;
import com.na.common.utils.ObjConversionUtil;
import com.na.core.mapper.ISysUserMapper;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.na.core.constant.Consts;

import java.util.List;

import com.na.core.repo.ISysUserRepo;
import com.na.entity.SysUserEntity;
import com.na.model.SysUserModel;
import com.na.params.SysUserParams;

/**
 * sys_user Repo实现类
 */
@Repository
public class SysUserRepoImpl extends ServiceImpl<ISysUserMapper, SysUserEntity> implements ISysUserRepo {

    @Override
    public SysUserModel saveObj(SysUserModel model) {
        SysUserEntity entity = ObjConversionUtil.copyProperties(model, SysUserEntity.class);
        save(entity);
        return ObjConversionUtil.copyProperties(entity, SysUserModel.class);
    }

    @Override
    public Boolean updateObj(SysUserModel model) {
        return super.updateById(ObjConversionUtil.copyProperties(model, SysUserEntity.class));
    }

    @Override
    public Boolean saveBatchObj(List<SysUserModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysUserModel> subList = modelList.subList(i, end);
            List<SysUserEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysUserEntity.class);
            saveBatch(entities);
        }
        return true;
    }

    @Override
    public Boolean updateBatch(List<SysUserModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysUserModel> subList = modelList.subList(i, end);
            List<SysUserEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysUserEntity.class);
            updateBatchById(entities);
        }
        return true;
    }

    @Override
    public IPage<SysUserModel> queryPaging(SysUserParams params) {
        Page<SysUserEntity> page = new Page<>(params.getCurrentPage(), params.getPageSize());
        IPage<SysUserEntity> iPage = baseMapper.queryPaging(page, params);
        return BasePagesDomain.iPages(iPage, ObjConversionUtil.copyPropertiesList(iPage.getRecords(), SysUserModel.class));
    }

    @Override
    public SysUserModel queryUserByUserNameOrPhone(String userName) {
        return ObjConversionUtil.copyProperties(baseMapper.queryUserByUserNameOrPhone(userName), SysUserModel.class);
    }

    @Override
    public SysUserModel queryUserByUserId(Long userId) {
        return ObjConversionUtil.copyProperties(getById(userId), SysUserModel.class);
    }

    @Override
    public SysUserModel queryUserByUserName(String userName) {
        return ObjConversionUtil.copyProperties(baseMapper.queryUserByUserName(userName), SysUserModel.class);
    }

    @Override
    public SysUserModel queryUserByPhone(String phone) {
        return ObjConversionUtil.copyProperties(baseMapper.queryUserByPhone(phone), SysUserModel.class);
    }

}