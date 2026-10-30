package com.na.core.mapper;

import com.na.entity.SysDeptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;

/**
 * sys_dept Mapper接口
 */
@Mapper
public interface ISysDeptMapper extends BaseMapper<SysDeptEntity> {

    /** 查询未删除的全部部门（组树用） */
    List<SysDeptEntity> queryAll();

    /**
     * 按名称查重。
     * 唯一性由应用层保证，且刻意不过滤 is_del：
     * 已删除的部门名不允许重建。
     */
    SysDeptEntity queryByName(@Param("name") String name);

    /** 按上级部门ID查询未删除的子部门 */
    List<SysDeptEntity> queryByParentId(@Param("parentId") Long parentId);
}
