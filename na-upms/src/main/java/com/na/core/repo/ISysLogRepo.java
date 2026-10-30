package com.na.core.repo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.na.model.SysLogModel;
import com.na.params.QueryLogParams;

/**
 * @author pg
 * @date 2025年11月06日
 */
public interface ISysLogRepo {
    IPage<SysLogModel> querySysLogPaging(QueryLogParams params);

    Boolean saveObj(SysLogModel model);
}
