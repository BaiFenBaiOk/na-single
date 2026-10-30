package com.na.admin.controller.vo.res.sysmenu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResMenuBtnVO {
    private Long id;
    private String menuName;
    private String perms;
    private String icon;

    private Long parentId;
}
