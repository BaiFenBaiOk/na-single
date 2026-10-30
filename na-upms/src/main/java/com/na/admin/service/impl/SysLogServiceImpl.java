package com.na.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.na.admin.controller.vo.req.logs.ReqLogPagingVO;
import com.na.admin.controller.vo.res.logs.ResLogPagingVO;
import com.na.admin.service.ISysLogService;
import com.na.common.base.BasePagesDomain;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.utils.IpParseUtil;
import com.na.core.repo.ISysLogRepo;
import com.na.model.SysLogModel;
import com.na.params.QueryLogParams;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * @author pg
 * @date 2025年11月06日
 */
@Service
public class SysLogServiceImpl implements ISysLogService {
    @Autowired
    private ISysLogRepo sysLogRepo;

    @Override
    public Result querySysLogPaging(ReqLogPagingVO pagingVO) {
        QueryLogParams params = QueryLogParams.builder()
                .userId(pagingVO.getUserId())
                .model(pagingVO.getModel())
                .operate(pagingVO.getOperate())
                .consumingType(pagingVO.getConsumingType())
                .consumingTime(pagingVO.getConsumingTime())
                .operateStartTime(pagingVO.getOperateStartTime())
                .operateEndTime(pagingVO.getOperateEndTime())
                .userName(pagingVO.getUserName())
                .build();
        params.setCurrentPage(pagingVO.getCurrentPage());
        params.setPageSize(pagingVO.getPageSize());
        List<ResLogPagingVO> list = new LinkedList<>();
        IPage<SysLogModel> modelIPage = sysLogRepo.querySysLogPaging(params);
        if(modelIPage != null && CollectionUtils.isNotEmpty(modelIPage.getRecords())){
            for (SysLogModel model : modelIPage.getRecords()){
                ResLogPagingVO resLogPagingVO = ResLogPagingVO.builder()
                        .id(model.getId())
                        .requestIp(model.getRequestIp())
                        .type(model.getType())
                        .userId(model.getUserId())
                        .description(model.getDescription())
                        .classPath(model.getClassPath())
                        .actionMethod(model.getActionMethod())
                        .requestUri(model.getRequestUri())
                        .ipAddress(IpParseUtil.parse(model.getRequestIp()))
                        .httpMethod(model.getHttpMethod())
                        .params(model.getParams())
                        .result(model.getResult())
                        .exDesc(model.getExDesc())
                        .exDetail(model.getExDetail())
                        .startTime(model.getStartTime())
                        .finishTime(model.getFinishTime())
                        .consumingTime(model.getConsumingTime())
                        .module(model.getModule())
                        .operate(model.getOperate())
                        .content(model.getContent())
                        .ua(model.getUa())
                        .operateTime(model.getOperateTime())
                        .userName(model.getUserName())
                        .nickName(model.getNickName())
                        .userFrom(model.getUserFrom())
                        .build();
                list.add(resLogPagingVO);
            }
        }
        return R.successNotMsg(BasePagesDomain.pages(modelIPage,list));
    }

}
