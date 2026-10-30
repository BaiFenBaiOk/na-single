package com.na.core.mapper;

import com.na.entity.SysUserEntity;
import com.na.params.SysUserParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
 * sys_user Mapper接口
 */
@Mapper
public interface ISysUserMapper extends BaseMapper<SysUserEntity> {

    /** 分页条件查询 */
    IPage<SysUserEntity> queryPaging(Page<SysUserEntity> page, @Param("params") SysUserParams params);

    SysUserEntity queryUserByUserNameOrPhone(String userName);

    SysUserEntity queryUserByUserName(String userName);

    SysUserEntity queryUserByPhone(String phone);
}