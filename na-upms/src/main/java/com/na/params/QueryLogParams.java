package com.na.params;

import com.na.common.base.BasePagesParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author pg
 * @date 2025年11月06日
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class QueryLogParams extends BasePagesParam {
    private Long userId;
    private String model;
    private String operate;
    private Integer consumingType;
    private Long consumingTime;
    private LocalDateTime operateStartTime;
    private LocalDateTime operateEndTime;
    private String userName;
}
