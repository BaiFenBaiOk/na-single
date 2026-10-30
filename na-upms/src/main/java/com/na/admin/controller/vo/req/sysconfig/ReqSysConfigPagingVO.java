package com.na.admin.controller.vo.req.sysconfig;

import com.na.common.base.BasePagesParam;
import lombok.Data;

@Data
public class ReqSysConfigPagingVO extends BasePagesParam {
    private Long fromId;
    private String code;
    private String fromCode;
}
