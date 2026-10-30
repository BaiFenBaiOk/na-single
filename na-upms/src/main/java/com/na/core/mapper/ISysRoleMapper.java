package com.na.core.mapper;

import com.na.entity.SysRoleEntity;
import com.na.params.SysRoleParams;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * sys_role Mapper接口
 */
@Mapper
public interface ISysRoleMapper extends BaseMapper<SysRoleEntity> {

    /** 分页条件查询 */
    IPage<SysRoleEntity> queryPaging(Page<SysRoleEntity> page, @Param("params") SysRoleParams params);

    List<SysRoleEntity> queryRoleByUserId(Long userId);

    SysRoleEntity queryRoleByRoleObj(SysRoleEntity sysRoleEntity);

    List<SysRoleEntity> queryRoleByRoleIds(@Param("roleIds") Collection<? extends Serializable> roleIds);
}