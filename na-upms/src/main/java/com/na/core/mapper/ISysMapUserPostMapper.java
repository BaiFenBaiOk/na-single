package com.na.core.mapper;

import com.na.entity.SysMapUserPostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;

/**
 * sys_map_user_post Mapper接口
 */
@Mapper
public interface ISysMapUserPostMapper extends BaseMapper<SysMapUserPostEntity> {

    /** 按用户ID查询已绑定岗位（带岗位名称） */
    List<SysMapUserPostEntity> queryByUserId(@Param("userId") Long userId);

    /** 统计某岗位下的用户绑定数量（删除岗位前的占用检查） */
    long countByPostId(@Param("postId") Long postId);
}
