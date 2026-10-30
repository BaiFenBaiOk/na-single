package com.na.core.repo;

import com.na.entity.SysUserEntity;
import com.na.model.SysUserModel;
import com.na.params.SysUserParams;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sys_user Repo服务接口
 */
public interface ISysUserRepo extends IService<SysUserEntity> {

    /** 新增对象 */
    SysUserModel saveObj(SysUserModel model);

    /** 修改对象 */
    Boolean updateObj(SysUserModel model);

    /** 批量新增对象 */
    Boolean saveBatchObj(List<SysUserModel> modelList);

    /** 批量修改对象 */
    Boolean updateBatch(List<SysUserModel> modelList);

    /** 分页条件查询 */
    IPage<SysUserModel> queryPaging(SysUserParams params);

    SysUserModel queryUserByUserNameOrPhone(String userName);

    SysUserModel queryUserByUserId(Long userId);

    SysUserModel queryUserByUserName(String userName);

    SysUserModel queryUserByPhone(String phone);
}