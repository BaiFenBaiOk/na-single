package com.na.core.repo;

import com.na.entity.SysMapUserDeptEntity;
import com.na.model.SysMapUserDeptModel;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sys_map_user_dept Repo服务接口
 */
public interface ISysMapUserDeptRepo extends IService<SysMapUserDeptEntity> {

    /** 新增对象 */
    Boolean saveObj(SysMapUserDeptModel model);

    /** 批量新增对象 */
    Boolean saveBatchObj(List<SysMapUserDeptModel> modelList);

    /** 按用户ID查询已绑定部门（带部门名称） */
    List<SysMapUserDeptModel> queryByUserId(Long userId);

    /** 删除某用户的全部部门绑定 */
    Boolean deleteByUserId(Long userId);

    /** 统计某部门下的用户绑定数量 */
    long countByDeptId(Long deptId);
}
