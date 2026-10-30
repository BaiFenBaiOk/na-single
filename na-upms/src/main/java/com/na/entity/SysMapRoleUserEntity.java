package com.na.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * sys_map_role_user数据库实体
 */
@Data
@TableName("sys_map_role_user")
public class SysMapRoleUserEntity {

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

}