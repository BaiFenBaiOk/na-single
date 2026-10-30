package com.na.admin.service;

import com.na.admin.controller.vo.req.dept.ReqSysDeptSaveVO;
import com.na.admin.controller.vo.req.dept.ReqSysDeptUpdateVO;
import com.na.common.result.Result;

/**
 * sys_dept Service接口
 */
public interface ISysDeptService {

    /** 部门树 */
    Result tree();

    /** 新增部门 */
    Result saveObj(ReqSysDeptSaveVO vo);

    /** 编辑部门 */
    Result updateObj(ReqSysDeptUpdateVO vo);

    /** 删除部门（逻辑删除；有子部门或有用户绑定则拒绝） */
    Result deleteObj(Long id);
}
