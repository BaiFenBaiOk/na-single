package com.na.core.mapper;

import com.na.entity.SysMapRoleUserEntity;
import com.na.params.SysMapRoleUserParams;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * sys_map_role_user Mapper接口
 */
@Mapper
public interface ISysMapRoleUserMapper extends BaseMapper<SysMapRoleUserEntity> {

    /** 分页条件查询 */
    IPage<SysMapRoleUserEntity> queryPaging(Page<SysMapRoleUserEntity> page, @Param("params") SysMapRoleUserParams params);

}