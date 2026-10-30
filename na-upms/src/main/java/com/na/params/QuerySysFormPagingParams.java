package com.na.params;

import com.na.common.base.BasePagesParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuerySysFormPagingParams extends BasePagesParam {
    private Long fromId;
    private String fromCode;
}
