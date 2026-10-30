package com.na.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.na.admin.controller.vo.req.sysconfig.*;
import com.na.admin.controller.vo.res.sysconfig.ResSysConfigPagingVO;
import com.na.admin.controller.vo.res.sysconfig.ResSysFormPagingVO;
import com.na.admin.service.ISystemConfigService;
import com.na.admin.utils.CaCheUtils;
import com.na.common.base.BasePagesDomain;
import com.na.common.constant.CacheSysConfigConst;
import com.na.common.dto.MyAuthDto;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.common.utils.CacheTemplateUtil;
import com.na.common.utils.TokenUtil;
import com.na.core.repo.ISystemConfigRepo;
import com.na.core.repo.ISystemFormRepo;
import com.na.model.SystemConfigModel;
import com.na.model.SystemFormModel;
import com.na.params.QuerySysFormPagingParams;
import jakarta.annotation.PostConstruct;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统配置服务：配置项为全局配置。
 *
 * @author pg
 * @date 2025年12月15日
 */
@Service
public class SystemConfigServiceImpl implements ISystemConfigService {

    @Autowired
    private ISystemFormRepo systemFormRepo;
    @Autowired
    private ISystemConfigRepo systemConfigRepo;
    @Autowired
    private CacheTemplateUtil cacheTemplateUtil;
    @Autowired
    private CaCheUtils caCheUtils;

    /**
     * 重启系统时加载缓存
     */
    @PostConstruct
    public void handleApplicationReady() {
        List<SystemConfigModel> configModels = systemConfigRepo.queryAll();
        if (configModels != null && !configModels.isEmpty()) {
            String key = caCheUtils.getCacheKey();
            for (SystemConfigModel config : configModels) {
                cacheTemplateUtil.hset(key, config.getCode().trim(), config.getValue().trim());
            }
        }
    }

    @Override
    public Result queryValueByName(ReqSystemConfigValueVO vo) {
        String keyCode = vo.getCode();
        String key = caCheUtils.getCacheKey();
        Object cacheValue = cacheTemplateUtil.hget(key, keyCode);
        if (cacheValue != null && StringUtils.isNotBlank(cacheValue.toString())) {
            return R.successNotMsg(cacheValue.toString());
        }

        SystemConfigModel model = systemConfigRepo.queryByCode(keyCode);
        if (model == null || StringUtils.isBlank(model.getValue())) {
            // 防止缓存穿透
            cacheTemplateUtil.hset(key, keyCode, "");
            return R.successNotMsg("");
        }

        String value = model.getValue();
        cacheTemplateUtil.hset(key, keyCode.trim(), value.trim());
        return R.successNotMsg(value);
    }

    @Override
    public Result queryValueMapByName(ReqSystemConfigValueMapVO vo) {
        List<String> codes = vo.getCodes();
        String key = caCheUtils.getCacheKey();

        Map<String, Object> result = new HashMap<>(codes.size());

        // 1. 批量查缓存
        Map<String, Object> rawCacheMap = cacheTemplateUtil.hmget(key, codes);
        final Map<String, Object> cacheMap = rawCacheMap == null ? new HashMap<>() : rawCacheMap;

        // 2. 找出缓存未命中的 key
        List<String> missKeys = codes.stream()
                .filter(code -> {
                    Object v = cacheMap.get(code);
                    return v == null || StringUtils.isBlank(v.toString());
                })
                .collect(Collectors.toList());

        // 3. 批量查 DB
        Map<String, String> dbMap = new HashMap<>();
        if (!missKeys.isEmpty()) {
            List<SystemConfigModel> list = systemConfigRepo.queryByCodes(missKeys);
            dbMap = list.stream()
                    .filter(m -> StringUtils.isNotBlank(m.getValue()))
                    .collect(Collectors.toMap(
                            SystemConfigModel::getCode,
                            SystemConfigModel::getValue,
                            (left, right) -> left
                    ));
        }

        // 4. 回写缓存（包括空值，防穿透）
        if (!missKeys.isEmpty()) {
            Map<String, Object> cacheBack = new HashMap<>();
            for (String missKey : missKeys) {
                cacheBack.put(missKey.trim(), dbMap.getOrDefault(missKey, "").trim());
            }
            cacheTemplateUtil.hmSet(key, cacheBack);
        }

        // 5. 组装返回结果
        for (String code : codes) {
            Object v = cacheMap.get(code);
            if (v == null || StringUtils.isBlank(v.toString())) {
                v = dbMap.get(code);
            }
            result.put(code, v);
        }

        return R.successNotMsg(result);
    }

    @Override
    public Result queryValueAll() {
        String key = caCheUtils.getCacheKey();
        Map<String, String> cacheMap = cacheTemplateUtil.hgetAll(key);

        if (cacheMap != null && !cacheMap.isEmpty()) {
            return R.successNotMsg(cacheMap);
        }

        // 缓存未命中：查数据库并一次性回写
        List<SystemConfigModel> configModels = systemConfigRepo.queryAll();
        if (configModels == null || configModels.isEmpty()) {
            return R.successNotMsg(Collections.emptyMap());
        }

        Map<String, Object> dbMap = new HashMap<>(configModels.size());
        for (SystemConfigModel config : configModels) {
            if (StringUtils.isNotBlank(config.getCode())
                    && StringUtils.isNotBlank(config.getValue())) {
                dbMap.put(config.getCode().trim(), config.getValue().trim());
            }
        }

        if (!dbMap.isEmpty()) {
            cacheTemplateUtil.hmSet(key, dbMap);
        }
        return R.successNotMsg(dbMap);
    }

    @Override
    public Result queryPaging(ReqSysConfigPagingVO pagingVO) {
        QuerySysFormPagingParams params = QuerySysFormPagingParams.builder()
                .fromCode(pagingVO.getFromCode())
                .build();
        params.setCurrentPage(pagingVO.getCurrentPage());
        params.setPageSize(pagingVO.getPageSize());

        List<ResSysFormPagingVO> list = new ArrayList<>();
        IPage<SystemFormModel> modelIPage = systemFormRepo.queryPaging(params);
        if (modelIPage != null && CollectionUtils.isNotEmpty(modelIPage.getRecords())) {
            for (SystemFormModel model : modelIPage.getRecords()) {
                List<ResSysConfigPagingVO> listVo = new ArrayList<>();
                List<SystemConfigModel> configModels = systemConfigRepo.queryByFormId(model.getId());
                if (CollectionUtils.isNotEmpty(configModels)) {
                    for (SystemConfigModel configModel : configModels) {
                        listVo.add(ResSysConfigPagingVO.builder()
                                .id(configModel.getId())
                                .formId(configModel.getFormId())
                                .code(configModel.getCode())
                                .name(configModel.getName())
                                .value(configModel.getValue())
                                .remarks(configModel.getRemarks())
                                .isDel(configModel.getIsDel())
                                .build());
                    }
                }
                list.add(ResSysFormPagingVO.builder()
                        .id(model.getId())
                        .name(model.getName())
                        .code(model.getCode())
                        .list(listVo)
                        .build());
            }
        }
        return R.successNotMsg(BasePagesDomain.pages(modelIPage, list));
    }

    @Override
    public Result update(ReqSysConfigUpdateVO vo) {
        MyAuthDto authModel = TokenUtil.getUser();
        if (Objects.isNull(authModel)) {
            return R.error(Status.USER_NEED_AUTHORITIES);
        }
        SystemConfigModel model = systemConfigRepo.queryById(vo.getId());
        if (Objects.isNull(model)) {
            return R.error(Status.DATE_NOT_EXIST);
        }
        model.setValue(vo.getValue());
        model.setRemarks(vo.getRemarks());
        systemConfigRepo.updateObj(model);
        cacheTemplateUtil.hset(caCheUtils.getCacheKey(),
                model.getCode().trim(), model.getValue().trim());
        return R.successNotMsg();
    }

    @Override
    public Result save(ReqSysConfigSaveVO vo) {
        SystemFormModel sysFormModel = systemFormRepo.queryById(vo.getFormId());
        if (sysFormModel == null) {
            return R.error("表单不存在");
        }
        SystemConfigModel model = systemConfigRepo.queryByCode(vo.getCode());
        if (model != null) {
            return R.error("配置已存在");
        }
        MyAuthDto authModel = TokenUtil.getUser();
        if (Objects.isNull(authModel)) {
            return R.error(Status.USER_NEED_AUTHORITIES);
        }

        model = SystemConfigModel.builder()
                .formId(vo.getFormId())
                .code(vo.getCode())
                .name(vo.getName())
                .value(vo.getValue())
                .remarks(vo.getRemarks())
                .isDel(false)
                .build();
        systemConfigRepo.saveObj(model);
        cacheTemplateUtil.hset(caCheUtils.getCacheKey(),
                vo.getCode().trim(), vo.getValue().trim());
        return R.successNotMsg();
    }

    @Override
    public Result deleteObj(Long id) {
        SystemConfigModel model = SystemConfigModel.builder()
                .id(id)
                .isDel(true)
                .build();
        systemConfigRepo.updateObj(model);
        return R.successNotMsg();
    }
}
