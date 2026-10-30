package com.na.core.repo.impl;

import com.na.common.utils.ObjConversionUtil;
import com.na.core.constant.Consts;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

import com.na.core.repo.ISysMapUserDeptRepo;
import com.na.core.mapper.ISysMapUserDeptMapper;
import com.na.entity.SysMapUserDeptEntity;
import com.na.model.SysMapUserDeptModel;

/**
 * sys_map_user_dept Repo实现类
 */
@Repository
public class SysMapUserDeptRepoImpl extends ServiceImpl<ISysMapUserDeptMapper, SysMapUserDeptEntity> implements ISysMapUserDeptRepo {

    @Override
    public Boolean saveObj(SysMapUserDeptModel model) {
        return super.save(ObjConversionUtil.copyProperties(model, SysMapUserDeptEntity.class));
    }

    @Override
    public Boolean saveBatchObj(List<SysMapUserDeptModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysMapUserDeptModel> subList = modelList.subList(i, end);
            List<SysMapUserDeptEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysMapUserDeptEntity.class);
            saveBatch(entities);
        }
        return true;
    }

    @Override
    public List<SysMapUserDeptModel> queryByUserId(Long userId) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryByUserId(userId), SysMapUserDeptModel.class);
    }

    @Override
    public Boolean deleteByUserId(Long userId) {
        return lambdaUpdate().eq(SysMapUserDeptEntity::getUserId, userId).remove();
    }

    @Override
    public long countByDeptId(Long deptId) {
        return baseMapper.countByDeptId(deptId);
    }

}
