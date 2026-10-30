package com.na.core.repo.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.na.common.base.BasePagesDomain;
import com.na.common.utils.ObjConversionUtil;
import com.na.core.mapper.ISystemFormMapper;
import com.na.core.repo.ISystemFormRepo;
import com.na.entity.SystemFormEntity;
import com.na.model.SystemFormModel;
import com.na.params.QuerySysFormPagingParams;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pg
 * @date 2025年12月15日
 */
@Repository
public class SystemFormRepoImpl extends ServiceImpl<ISystemFormMapper, SystemFormEntity> implements ISystemFormRepo {
    @Override
    public IPage<SystemFormModel> queryPaging(QuerySysFormPagingParams params) {
        Page<SystemFormEntity> page = new Page<>(params.getCurrentPage(), params.getPageSize());
        IPage<SystemFormEntity> iPage = baseMapper.queryPaging(page, params);
        if (iPage != null && CollectionUtils.isNotEmpty(iPage.getRecords())) {
            return BasePagesDomain.iPages(iPage, ObjConversionUtil.copyPropertiesList(iPage.getRecords(), SystemFormModel.class));
        }
        return BasePagesDomain.iPages(iPage,new ArrayList<>());
    }

    @Override
    public SystemFormModel queryById(Long formId) {
        SystemFormEntity entity = baseMapper.selectById(formId);
        if (entity == null) {
            return null;
        }
        return ObjConversionUtil.copyProperties(entity, SystemFormModel.class);
    }

    @Override
    public void saveBatchObj(List<SystemFormModel> systemFormModels) {
        saveBatch(ObjConversionUtil.copyPropertiesList(systemFormModels, SystemFormEntity.class));
    }

    @Override
    public SystemFormModel saveObj(SystemFormModel formModel) {
        SystemFormEntity entity = ObjConversionUtil.copyProperties(formModel, SystemFormEntity.class);
        baseMapper.insert(entity);
        return ObjConversionUtil.copyProperties(entity, SystemFormModel.class);
    }

}
