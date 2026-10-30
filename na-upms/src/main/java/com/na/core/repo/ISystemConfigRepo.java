package com.na.core.repo;

import com.na.model.SystemConfigModel;

import java.util.List;

/**
 * system_config 配置项 Repo 接口
 */
public interface ISystemConfigRepo {

    List<SystemConfigModel> queryAll();

    SystemConfigModel queryById(Long id);

    void updateObj(SystemConfigModel model);

    SystemConfigModel saveObj(SystemConfigModel model);

    List<SystemConfigModel> queryByCodes(List<String> codes);

    List<SystemConfigModel> queryByFormId(Long formId);

    void saveBatchObj(List<SystemConfigModel> models);

    SystemConfigModel queryByCode(String code);

}
