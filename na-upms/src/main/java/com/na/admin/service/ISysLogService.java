package com.na.admin.service;

import com.na.admin.controller.vo.req.logs.ReqLogPagingVO;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.model.SysLogModel;

/**
 * @author pg
 * @date 2025年11月06日
 */
public interface ISysLogService {
    Result querySysLogPaging(ReqLogPagingVO pagingVO);

}
