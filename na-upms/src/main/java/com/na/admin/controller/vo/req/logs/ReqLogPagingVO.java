package com.na.admin.controller.vo.req.logs;

import com.na.common.base.BasePagesParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author pg
 * @date 2025年11月06日
 */
@Schema
@Data
public class ReqLogPagingVO extends BasePagesParam {
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "模块名称")
    private String model;
    @Schema(description = "操作名称")
    private String operate;
    @Schema(description = "消耗时间类型，大于1，大于等于2，等于3，小于4，小于等于5")
    private Integer consumingType = 1;
    @Schema(description = "消耗时间")
    private Long consumingTime;
    @Schema(description = "操作开始时间")
    private LocalDateTime operateStartTime;
    @Schema(description = "操作结束时间")
    private LocalDateTime operateEndTime;
    @Schema(description = "用户名称")
    private String userName;
}
