package com.na.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.na.entity.SysLogEntity;
import com.na.params.QueryLogParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author pg
 * @date 2025年11月06日
 */
@Mapper
public interface ISysLogMapper extends BaseMapper<SysLogEntity> {
    IPage<SysLogEntity> querySysLogPaging(Page<SysLogEntity> page,
                                          @Param("params") QueryLogParams params);
}
