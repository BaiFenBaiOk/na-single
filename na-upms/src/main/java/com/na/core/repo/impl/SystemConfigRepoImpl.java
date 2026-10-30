package com.na.core.repo.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.na.common.utils.ObjConversionUtil;
import com.na.core.constant.Consts;
import com.na.core.mapper.ISystemConfigMapper;
import com.na.core.repo.ISystemConfigRepo;
import com.na.entity.SystemConfigEntity;
import com.na.model.SystemConfigModel;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * @author pg
 * @date 2025年12月15日
 */
@Repository
public class SystemConfigRepoImpl extends ServiceImpl<ISystemConfigMapper, SystemConfigEntity> implements ISystemConfigRepo {
    @Override
    public List<SystemConfigModel> queryAll() {
        List<SystemConfigEntity> entities = baseMapper.queryAll();
        return ObjConversionUtil.copyPropertiesList(entities, SystemConfigModel.class);
    }

    @Override
    public SystemConfigModel queryById(Long id) {
        return ObjConversionUtil.copyProperties( baseMapper.selectById(id), SystemConfigModel.class);
    }

    @Override
    public void updateObj(SystemConfigModel model) {
        baseMapper.updateById(ObjConversionUtil.copyProperties(model, SystemConfigEntity.class));
    }

    @Override
    public SystemConfigModel saveObj(SystemConfigModel model) {
        SystemConfigEntity entity = ObjConversionUtil.copyProperties(model, SystemConfigEntity.class);
        baseMapper.insert(entity);
        return ObjConversionUtil.copyProperties(entity, SystemConfigModel.class);
    }

    @Override
    public List<SystemConfigModel> queryByCodes(List<String> codes) {
        return ObjConversionUtil.copyPropertiesList(baseMapper.queryByCodes(codes), SystemConfigModel.class);
    }

    @Override
    public List<SystemConfigModel> queryByFormId(Long formId) {
        List<SystemConfigEntity> entities = baseMapper.queryByFormId(formId);
        return ObjConversionUtil.copyPropertiesList(entities, SystemConfigModel.class);
    }

    @Override
    public void saveBatchObj(List<SystemConfigModel> models) {
        final int BATCH_SIZE = Consts.BATCH_SIZE;

        for (int i = 0; i < models.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, models.size());

            List<SystemConfigModel> subList = models.subList(i, end);

            // 只 copy 当前批次，避免内存暴涨
            List<SystemConfigEntity> entities =
                    ObjConversionUtil.copyPropertiesList(subList, SystemConfigEntity.class);

            updateBatchById(entities);
        }
    }

    @Override
    public SystemConfigModel queryByCode(String code) {
        return ObjConversionUtil.copyProperties(baseMapper.queryByCode(code), SystemConfigModel.class);
    }

}
