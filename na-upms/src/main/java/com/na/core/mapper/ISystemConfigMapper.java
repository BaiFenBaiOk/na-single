package com.na.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.na.entity.SystemConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ISystemConfigMapper extends BaseMapper<SystemConfigEntity> {
    List<SystemConfigEntity> queryAll();

    SystemConfigEntity queryByCode(@Param("code") String code);

    List<SystemConfigEntity> queryByCodes(@Param("params") List<String> codes);

    List<SystemConfigEntity> queryByFormId(@Param("formId") Long formId);

}
