package com.na.core.repo;

import com.na.entity.SysMapUserPostEntity;
import com.na.model.SysMapUserPostModel;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sys_map_user_post Repo服务接口
 */
public interface ISysMapUserPostRepo extends IService<SysMapUserPostEntity> {

    /** 新增对象 */
    Boolean saveObj(SysMapUserPostModel model);

    /** 批量新增对象 */
    Boolean saveBatchObj(List<SysMapUserPostModel> modelList);

    /** 按用户ID查询已绑定岗位（带岗位名称） */
    List<SysMapUserPostModel> queryByUserId(Long userId);

    /** 删除某用户的全部岗位绑定 */
    Boolean deleteByUserId(Long userId);

    /** 统计某岗位下的用户绑定数量 */
    long countByPostId(Long postId);
}
