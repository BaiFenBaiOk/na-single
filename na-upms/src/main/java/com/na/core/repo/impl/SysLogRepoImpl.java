package com.na.core.repo.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.na.common.base.BasePagesDomain;
import com.na.common.utils.ObjConversionUtil;
import com.na.core.mapper.ISysLogMapper;
import com.na.core.repo.ISysLogRepo;
import com.na.entity.SysLogEntity;
import com.na.model.SysLogModel;
import com.na.params.QueryLogParams;
import org.springframework.stereotype.Repository;

/**
 * @author pg
 * @date 2025年11月06日
 */
@Repository
public class SysLogRepoImpl extends ServiceImpl<ISysLogMapper, SysLogEntity> implements ISysLogRepo {

    @Override
    public IPage<SysLogModel> querySysLogPaging(QueryLogParams params) {
        Page<SysLogEntity> page = new Page<>(params.getCurrentPage(), params.getPageSize());
        IPage<SysLogEntity> iPage = baseMapper.querySysLogPaging(page,params);
        return BasePagesDomain.iPages(iPage, ObjConversionUtil.copyPropertiesList(iPage.getRecords(), SysLogModel.class));
    }

    @Override
    public Boolean saveObj(SysLogModel model) {
        return baseMapper.insert(ObjConversionUtil.copyProperties(model,SysLogEntity.class)) > 0;
    }
}
