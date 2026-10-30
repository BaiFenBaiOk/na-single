package com.na.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * sys_map_role_menu数据库实体
 */
@Data
@TableName("sys_map_role_menu")
public class SysMapRoleMenuEntity {

    /**  */
    private Long roleId;

    /**  */
    private Long menuId;

}