package com.na.admin.controller.vo.res.logs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema
@Data
@Builder
public class ResLogPagingVO {
    @Schema(description = "日志ID")
    private String id;
    @Schema(description = "IP地址")
    private String requestIp;
    @Schema(description = "操作类型 OPT EX")
    private String type;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "api描述信息")
    private String description;
    @Schema(description = "类名")
    private String classPath;
    @Schema(description = "方法名")
    private String actionMethod;
    @Schema(description = "请求api")
    private String requestUri;
    private List<String> ipAddress;
    @Schema(description = "请求方式")
    private String httpMethod;
    @Schema(description = "请求参数")
    private String params;
    @Schema(description = "结果")
    private String result;
    @Schema(description = "异常描述")
    private String exDesc;
    @Schema(description = "异常对象")
    private String exDetail;
    @Schema(description = "请求时间")
    private LocalDateTime startTime;
    @Schema(description = "响应时间")
    private LocalDateTime finishTime;
    @Schema(description = "消耗时间")
    private Long consumingTime;
    @Schema(description = "模块名")
    private String module;
    @Schema(description = "操作名")
    private String operate;
    @Schema(description = "其他描述信息")
    private String content;
    @Schema(description = "浏览器标识")
    private String ua;
    @Schema(description = "操作时间")
    private LocalDateTime operateTime;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "用户昵称")
    private String nickName;
    @Schema(description = "用户来源 0授权系统 1外部")
    private Integer userFrom;
}
