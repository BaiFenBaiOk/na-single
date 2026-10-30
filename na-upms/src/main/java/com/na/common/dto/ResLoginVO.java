package com.na.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResLoginVO {
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "用户昵称")
    private String nickName;
    @Schema(description = "用户类型")
    private String userType;
    @Schema(description = "token")
    private String token;
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
    @Schema(description = "登录地点")
    private String loginIp;
    @Schema(description = "登录地点")
    private List<String> loginAddress;

    private String email;
    private String phone;
    private String avatar;

    private String deptName;
    private Set<String> requestUriSet;

}
