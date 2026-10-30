package com.na.core.repo.impl;

import com.na.common.utils.ObjConversionUtil;
import com.na.core.mapper.ISysDeptMapper;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

import com.na.core.repo.ISysDeptRepo;
import com.na.entity.SysDeptEntity;
import com.na.model.SysDeptModel;

/**
 * sys_dept Repo实现类
 */
@Repository
public class SysDeptRepoImpl extends ServiceImpl<ISysDeptMapper, SysDeptEntity> implements ISysDeptRepo {

    @Override
    public SysDeptModel saveObj(SysDeptModel model) {
        SysDeptEntity entity = ObjConversionUtil.copyProperties(model, SysDeptEntity.class);
        save(entity);
        return ObjConversionUtil.copyProperties(entity, SysDeptModel.class);
    }

    @Override
    public Boolean updateObj(SysDeptModel model) {
        return super.updateById(ObjConversionUtil.copyProperties(model, SysDeptEntity.class));
    }

    @Override
    public SysDeptModel queryDeptById(Long id) {
        return ObjConversionUtil.copyProperties(getById(id), SysDeptModel.class);
    }

    @Override
    public List<SysDeptModel> queryDeptsAll() {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryAll(), SysDeptModel.class);
    }

    @Override
    public SysDeptModel queryDeptByName(String name) {
        return ObjConversionUtil.copyProperties(baseMapper.queryByName(name), SysDeptModel.class);
    }

    @Override
    public List<SysDeptModel> queryDeptsByParentId(Long parentId) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryByParentId(parentId), SysDeptModel.class);
    }

}
