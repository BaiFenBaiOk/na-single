package com.na.params;

import com.na.common.base.BasePagesParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * @author pg
 * @date 2025年11月06日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMenuParams extends BasePagesParam {
    private List<Long> ids;
    private Set<String> menuTypes;
    private Long parentId;
    private String menuName;
}
