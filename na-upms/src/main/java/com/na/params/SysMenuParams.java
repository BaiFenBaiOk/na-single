package com.na.params;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import com.na.common.base.BasePagesParam;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * sys_menu查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysMenuParams extends BasePagesParam {

    /** 菜单ID */
    private Long id;

    /**  */
    private String name;

    /** 菜单名称 */
    private String menuName;

    /** 父菜单ID */
    private Long parentId;

    /** 显示顺序 */
    private Integer orderNum;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** api */
    private String requestUri;

    /** 权限标识 */
    private String perms;

    /** 路由参数 */
    private String query;

    /** 是否为外链（0是 1否） */
    private Integer isFrame;

    /** 是否缓存（0缓存 1不缓存） */
    private Integer isCache;

    /** 菜单类型（M目录 C菜单 F按钮 Aapi） */
    private String menuType;

    /** 菜单状态（0显示 1隐藏） */
    private String visible;

    /** 菜单状态（0正常 1停用） */
    private String status;

    /** 菜单图标 */
    private String icon;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /** 菜单类型（00系统菜单）03普通 */
    private String sysMenuType;

    /**  */
    private Boolean isDel;

}