package com.na.core.mapper;

import com.na.entity.SysMapRoleMenuEntity;
import com.na.params.SysMapRoleMenuParams;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sys_map_role_menu Mapper接口
 */
@Mapper
public interface ISysMapRoleMenuMapper extends BaseMapper<SysMapRoleMenuEntity> {

    /** 分页条件查询 */
    IPage<SysMapRoleMenuEntity> queryPaging(Page<SysMapRoleMenuEntity> page, @Param("params") SysMapRoleMenuParams params);

    /** 查询指定角色已绑定的菜单 */
    List<SysMapRoleMenuEntity> queryAllRoleMenuByRoleId(@Param("roleId") Long roleId);

    /** 删除指定角色的全部菜单绑定 */
    int deleteMapRoleMenuByRoleId(@Param("roleId") Long roleId);
}
