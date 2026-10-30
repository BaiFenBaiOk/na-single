package com.na.admin.controller.vo.res.sysrole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResUserRoleVO {
    private Long id;
    private String roleName;
    private String roleCode;
}
