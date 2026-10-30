package com.na.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfigModel {
    private Long id;

    private Long formId;
    private String code;
    private String name;
    private String value;
    private String remarks;
    private Boolean isDel;
}
