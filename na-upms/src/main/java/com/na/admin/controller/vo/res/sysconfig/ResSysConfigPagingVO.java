package com.na.admin.controller.vo.res.sysconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResSysConfigPagingVO {
    private Long id;

    private Long formId;
    private String code;
    private String name;
    private String value;
    private String remarks;
    private Boolean isDel;
}
