package com.na.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("system_form")
public class SystemFormEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String code;
}
