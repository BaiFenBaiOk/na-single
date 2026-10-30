package com.na.admin.service;

import com.na.admin.controller.vo.req.sysconfig.*;
import com.na.common.result.Result;

/**
 * @author pg
 * @date 2025年12月15日
 */
public interface ISystemConfigService {
    Result queryValueByName(ReqSystemConfigValueVO vo);

    Result queryValueMapByName(ReqSystemConfigValueMapVO vo);

    /** 查询全部配置 value */
    Result queryValueAll();

    Result queryPaging(ReqSysConfigPagingVO pagingVO);

    Result update(ReqSysConfigUpdateVO vo);

    Result save(ReqSysConfigSaveVO vo);

    Result deleteObj(Long id);

}
