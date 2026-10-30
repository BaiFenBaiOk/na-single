package com.na.admin.service;

import com.na.admin.controller.vo.req.post.ReqSysPostPagingVO;
import com.na.admin.controller.vo.req.post.ReqSysPostSaveVO;
import com.na.admin.controller.vo.req.post.ReqSysPostUpdateVO;
import com.na.common.result.Result;

/**
 * sys_post Service接口
 */
public interface ISysPostService {

    /** 岗位分页查询 */
    Result queryPaging(ReqSysPostPagingVO pagingVO);

    /** 新增岗位 */
    Result saveObj(ReqSysPostSaveVO vo);

    /** 编辑岗位 */
    Result updateObj(ReqSysPostUpdateVO vo);

    /** 删除岗位（逻辑删除；有用户绑定则拒绝） */
    Result deleteObj(Long id);
}
