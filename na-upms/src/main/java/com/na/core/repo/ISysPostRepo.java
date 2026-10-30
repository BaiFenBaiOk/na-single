package com.na.core.repo;

import com.na.entity.SysPostEntity;
import com.na.model.SysPostModel;
import com.na.params.SysPostParams;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sys_post Repo服务接口
 */
public interface ISysPostRepo extends IService<SysPostEntity> {

    /** 新增对象 */
    SysPostModel saveObj(SysPostModel model);

    /** 修改对象 */
    Boolean updateObj(SysPostModel model);

    /** 分页条件查询 */
    IPage<SysPostModel> queryPaging(SysPostParams params);

    /** 按ID查询岗位 */
    SysPostModel queryPostById(Long id);

    /** 按编码查重（不过滤 is_del） */
    SysPostModel queryPostByCode(String code);

    /** 按名称查重（不过滤 is_del） */
    SysPostModel queryPostByName(String name);

    /** 按ID集合查询未删除的岗位 */
    List<SysPostModel> queryPostsByIds(List<Long> ids);
}
