package com.na.core.repo;

import com.na.entity.SysDeptEntity;
import com.na.model.SysDeptModel;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sys_dept Repo服务接口
 */
public interface ISysDeptRepo extends IService<SysDeptEntity> {

    /** 新增对象 */
    SysDeptModel saveObj(SysDeptModel model);

    /** 修改对象 */
    Boolean updateObj(SysDeptModel model);

    /** 按ID查询部门 */
    SysDeptModel queryDeptById(Long id);

    /** 查询未删除的全部部门（组树用） */
    List<SysDeptModel> queryDeptsAll();

    /** 按名称查重（不过滤 is_del） */
    SysDeptModel queryDeptByName(String name);

    /** 按上级部门ID查询未删除的子部门 */
    List<SysDeptModel> queryDeptsByParentId(Long parentId);
}
