package com.na.admin.controller.vo.res.sysuser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema
@Data
@Builder
public class ResUserInfoVO {
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "用户昵称")
    private String nickName;
    @Schema(description = "用户email")
    private String email;
    @Schema(description = "用户联系电话")
    private String phone;
    @Schema(description = "用户创建时间")
    private LocalDateTime createTime;
    @Schema(description = "用户开启状态")
    private Boolean status;
    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "用户角色")
    private Set<String> roleNames;
    @Schema(description = "用户角色ID")
    private Set<Long> roleIds;

    @Schema(description = "用户部门")
    private Set<String> deptName;

    @Schema(description = "用户岗位")
    private Set<String> postName;
}
