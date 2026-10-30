package com.na.core.mapper;

import com.na.entity.SysMenuEntity;
import com.na.params.QueryMenuParams;
import com.na.params.SysMenuParams;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * sys_menu Mapper接口
 */
@Mapper
public interface ISysMenuMapper extends BaseMapper<SysMenuEntity> {

    /** 分页条件查询 */
    IPage<SysMenuEntity> queryPaging(Page<SysMenuEntity> page, @Param("params") SysMenuParams params);

    List<SysMenuEntity> queryMenuAll(@Param("params") QueryMenuParams params);

    List<SysMenuEntity> selectMenuTreeByUserId(Long userId);

    List<SysMenuEntity> queryMenuByUserId(Long userId);

    List<SysMenuEntity> selectMenuTreeAll(@Param("params")Collection<? extends Serializable> types);

    List<SysMenuEntity> getMenuBtnByParentId(Long parentId);

    List<SysMenuEntity> getMenuBtnByParentIdAndUserId(@Param("parentId") Long parentId,@Param("userId") Long userId);
}