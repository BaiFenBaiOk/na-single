package com.na.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("system_config")
public class SystemConfigEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long formId;
    private String code;
    private String name;
    private String value;
    private String remarks;
    private Boolean isDel;
}
