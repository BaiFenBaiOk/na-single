package com.na.core.mapper;

import com.na.entity.SysMapUserDeptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;

/**
 * sys_map_user_dept Mapper接口
 */
@Mapper
public interface ISysMapUserDeptMapper extends BaseMapper<SysMapUserDeptEntity> {

    /** 按用户ID查询已绑定部门（带部门名称） */
    List<SysMapUserDeptEntity> queryByUserId(@Param("userId") Long userId);

    /** 统计某部门下的用户绑定数量（删除部门前的占用检查） */
    long countByDeptId(@Param("deptId") Long deptId);
}
