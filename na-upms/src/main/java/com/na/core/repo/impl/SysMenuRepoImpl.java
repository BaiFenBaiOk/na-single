package com.na.core.repo.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.na.params.QueryMenuParams;
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
import com.na.core.repo.ISysMenuRepo;
import com.na.core.mapper.ISysMenuMapper;
import com.na.entity.SysMenuEntity;
import com.na.model.SysMenuModel;
import com.na.params.SysMenuParams;

/**
 * sys_menu Repo实现类
 */
@Repository
public class SysMenuRepoImpl extends ServiceImpl<ISysMenuMapper, SysMenuEntity> implements ISysMenuRepo {

    @Override
    public Boolean saveObj(SysMenuModel model) {
        return super.save(ObjConversionUtil.copyProperties(model, SysMenuEntity.class));
    }

    @Override
    public Boolean updateObj(SysMenuModel model) {
        return super.updateById(ObjConversionUtil.copyProperties(model, SysMenuEntity.class));
    }

    @Override
    public Boolean saveBatchObj(List<SysMenuModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysMenuModel> subList = modelList.subList(i, end);
            List<SysMenuEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysMenuEntity.class);
            saveBatch(entities);
        }
        return true;
    }

    @Override
    public Boolean updateBatch(List<SysMenuModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysMenuModel> subList = modelList.subList(i, end);
            List<SysMenuEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysMenuEntity.class);
            updateBatchById(entities);
        }
        return true;
    }

    @Override
    public IPage<SysMenuModel> queryPaging(SysMenuParams params) {
        Page<SysMenuEntity> page = new Page<>(params.getCurrentPage(), params.getPageSize());
        IPage<SysMenuEntity> iPage = baseMapper.queryPaging(page, params);
        return BasePagesDomain.iPages(iPage, ObjConversionUtil.copyPropertiesList(iPage.getRecords(), SysMenuModel.class));
    }

    @Override
    public List<SysMenuModel> queryMenuAll(QueryMenuParams params) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryMenuAll(params), SysMenuModel.class);
    }

    @Override
    public List<SysMenuModel> selectMenuTreeByUserId(Long userId) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.selectMenuTreeByUserId(userId), SysMenuModel.class);
    }

    @Override
    public List<SysMenuModel> queryMenuAll() {
        return ObjConversionUtil.copyPropertiesList(baseMapper.selectList(new QueryWrapper<>()), SysMenuModel.class);
    }

    @Override
    public List<SysMenuModel> queryMenuByUserId(Long userId) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryMenuByUserId(userId), SysMenuModel.class);
    }

    @Override
    public List<SysMenuModel> selectMenuTreeAll(Collection<? extends Serializable> types) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.selectMenuTreeAll(types), SysMenuModel.class);
    }

    @Override
    public List<SysMenuModel> getMenuBtnByParentId(Long parentId) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.getMenuBtnByParentId(parentId), SysMenuModel.class);
    }

    @Override
    public List<SysMenuModel> getMenuBtnByParentIdAndUserId(Long parentId, Long userId) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.getMenuBtnByParentIdAndUserId(parentId,userId), SysMenuModel.class);
    }

    @Override
    public List<SysMenuModel> queryByIds(Collection<? extends Serializable> ids) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.selectByIds(ids), SysMenuModel.class);
    }

}