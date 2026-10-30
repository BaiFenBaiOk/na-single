package com.na.core.mapper;

import com.na.entity.SysPostEntity;
import com.na.params.SysPostParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
 * sys_post Mapper接口
 */
@Mapper
public interface ISysPostMapper extends BaseMapper<SysPostEntity> {

    /** 分页条件查询 */
    IPage<SysPostEntity> queryPaging(Page<SysPostEntity> page, @Param("params") SysPostParams params);

    /**
     * 按编码查重。
     * 不过滤 is_del：唯一索引不含 is_del，逻辑删除的行仍占索引位，
     * 过滤 is_del 会导致「应用层查不到 → 放行 → DB 撞唯一索引 → 500」。
     */
    SysPostEntity queryByCode(@Param("code") String code);

    /**
     * 按名称查重。
     * 同样不过滤 is_del：已删除的岗位名不允许重建。
     */
    SysPostEntity queryByName(@Param("name") String name);

    /** 按ID集合查询 */
    List<SysPostEntity> queryByIds(@Param("ids") List<Long> ids);
}
