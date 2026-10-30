package com.na.core.repo;

import com.na.entity.SysMapRoleUserEntity;
import com.na.model.SysMapRoleUserModel;
import com.na.params.SysMapRoleUserParams;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sys_map_role_user Repo服务接口
 */
public interface ISysMapRoleUserRepo extends IService<SysMapRoleUserEntity> {

    /** 新增对象 */
    Boolean saveObj(SysMapRoleUserModel model);

    /** 修改对象 */
    Boolean updateObj(SysMapRoleUserModel model);

    /** 批量新增对象 */
    Boolean saveBatchObj(List<SysMapRoleUserModel> modelList);

    /** 批量修改对象 */
    Boolean updateBatch(List<SysMapRoleUserModel> modelList);

    /** 分页条件查询 */
    IPage<SysMapRoleUserModel> queryPaging(SysMapRoleUserParams params);

    /** 根据用户ID删除角色 */
    Boolean deleteRoleByUserId(Long userId);

    /** 统计绑定到该角色的用户数量 */
    Long countByRoleId(Long roleId);

    /** 统计该用户绑定的角色数量 */
    Long countByUserId(Long userId);

    /** 保存角色用户关系 */
    Boolean saveRoleUserMap(List<SysMapRoleUserModel> models);
}