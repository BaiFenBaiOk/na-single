package com.na.core.repo.impl;

import com.na.common.utils.ObjConversionUtil;
import com.na.core.constant.Consts;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

import com.na.core.repo.ISysMapUserPostRepo;
import com.na.core.mapper.ISysMapUserPostMapper;
import com.na.entity.SysMapUserPostEntity;
import com.na.model.SysMapUserPostModel;

/**
 * sys_map_user_post Repo实现类
 */
@Repository
public class SysMapUserPostRepoImpl extends ServiceImpl<ISysMapUserPostMapper, SysMapUserPostEntity> implements ISysMapUserPostRepo {

    @Override
    public Boolean saveObj(SysMapUserPostModel model) {
        return super.save(ObjConversionUtil.copyProperties(model, SysMapUserPostEntity.class));
    }

    @Override
    public Boolean saveBatchObj(List<SysMapUserPostModel> modelList) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;
        for (int i = 0; i < modelList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, modelList.size());
            List<SysMapUserPostModel> subList = modelList.subList(i, end);
            List<SysMapUserPostEntity> entities = ObjConversionUtil.copyPropertiesList(subList, SysMapUserPostEntity.class);
            saveBatch(entities);
        }
        return true;
    }

    @Override
    public List<SysMapUserPostModel> queryByUserId(Long userId) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryByUserId(userId), SysMapUserPostModel.class);
    }

    @Override
    public Boolean deleteByUserId(Long userId) {
        return lambdaUpdate().eq(SysMapUserPostEntity::getUserId, userId).remove();
    }

    @Override
    public long countByPostId(Long postId) {
        return baseMapper.countByPostId(postId);
    }

}
