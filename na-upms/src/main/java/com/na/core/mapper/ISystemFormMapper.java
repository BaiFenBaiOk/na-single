package com.na.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.na.entity.SystemFormEntity;
import com.na.params.QuerySysFormPagingParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ISystemFormMapper extends BaseMapper<SystemFormEntity> {
    IPage<SystemFormEntity> queryPaging(Page<SystemFormEntity> page,
                                        @Param("params") QuerySysFormPagingParams params);
}
