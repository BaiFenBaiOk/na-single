package com.na.core.repo.impl;

import com.na.common.base.BasePagesDomain;
import com.na.common.utils.ObjConversionUtil;
import com.na.core.mapper.ISysPostMapper;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

import com.na.core.repo.ISysPostRepo;
import com.na.entity.SysPostEntity;
import com.na.model.SysPostModel;
import com.na.params.SysPostParams;

/**
 * sys_post Repo实现类
 */
@Repository
public class SysPostRepoImpl extends ServiceImpl<ISysPostMapper, SysPostEntity> implements ISysPostRepo {

    @Override
    public SysPostModel saveObj(SysPostModel model) {
        SysPostEntity entity = ObjConversionUtil.copyProperties(model, SysPostEntity.class);
        save(entity);
        return ObjConversionUtil.copyProperties(entity, SysPostModel.class);
    }

    @Override
    public Boolean updateObj(SysPostModel model) {
        return super.updateById(ObjConversionUtil.copyProperties(model, SysPostEntity.class));
    }

    @Override
    public IPage<SysPostModel> queryPaging(SysPostParams params) {
        Page<SysPostEntity> page = new Page<>(params.getCurrentPage(), params.getPageSize());
        IPage<SysPostEntity> iPage = baseMapper.queryPaging(page, params);
        return BasePagesDomain.iPages(iPage, ObjConversionUtil.copyPropertiesList(iPage.getRecords(), SysPostModel.class));
    }

    @Override
    public SysPostModel queryPostById(Long id) {
        return ObjConversionUtil.copyProperties(getById(id), SysPostModel.class);
    }

    @Override
    public SysPostModel queryPostByCode(String code) {
        return ObjConversionUtil.copyProperties(baseMapper.queryByCode(code), SysPostModel.class);
    }

    @Override
    public SysPostModel queryPostByName(String name) {
        return ObjConversionUtil.copyProperties(baseMapper.queryByName(name), SysPostModel.class);
    }

    @Override
    public List<SysPostModel> queryPostsByIds(List<Long> ids) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryByIds(ids), SysPostModel.class);
    }

}
