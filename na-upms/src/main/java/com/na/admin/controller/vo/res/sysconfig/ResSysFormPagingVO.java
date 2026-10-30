package com.na.admin.controller.vo.res.sysconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResSysFormPagingVO {
    private Long id;
    private String name;
    private String code;

    private List<ResSysConfigPagingVO> list;
}
