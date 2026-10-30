package com.na.core.repo;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.na.model.SystemFormModel;
import com.na.params.QuerySysFormPagingParams;

import java.util.List;

public interface ISystemFormRepo {

    IPage<SystemFormModel> queryPaging(QuerySysFormPagingParams params);

    SystemFormModel queryById(Long formId);

    void saveBatchObj(List<SystemFormModel> systemFormModels);

    SystemFormModel saveObj(SystemFormModel formModel);

}
