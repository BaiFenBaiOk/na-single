package com.na.admin.controller.vo.req.sysmenu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Schema(description = "SysMenu新增入参")
@Data
public class ReqSysMenuSaveVO {

    /** 菜单ID */
    @NotNull(message = "ID不能为空")
    @Schema(description = "菜单ID")
    private Long id;

    /**  */
    @Schema(description = "")
    private String name;

    /** 菜单名称 */
    @Schema(description = "菜单名称")
    private String menuName;

    /** 父菜单ID */
    @Schema(description = "父菜单ID")
    private Long parentId;

    /** 显示顺序 */
    @Schema(description = "显示顺序")
    private Integer orderNum;

    /** 路由地址 */
    @Schema(description = "路由地址")
    private String path;

    /** 组件路径 */
    @Schema(description = "组件路径")
    private String component;

    /** api */
    @Schema(description = "api")
    private String requestUri;

    /** 权限标识 */
    @Schema(description = "权限标识")
    private String perms;

    /** 路由参数 */
    @Schema(description = "路由参数")
    private String query;

    /** 是否为外链（0是 1否） */
    @Schema(description = "是否为外链（0是 1否）")
    private Integer isFrame;

    /** 是否缓存（0缓存 1不缓存） */
    @Schema(description = "是否缓存（0缓存 1不缓存）")
    private Integer isCache;

    /** 菜单类型（M目录 C菜单 F按钮 Aapi） */
    @Schema(description = "菜单类型（M目录 C菜单 F按钮 Aapi）")
    private String menuType;

    /** 菜单状态（0显示 1隐藏） */
    @Schema(description = "菜单状态（0显示 1隐藏）")
    private String visible;

    /** 菜单状态（0正常 1停用） */
    @Schema(description = "菜单状态（0正常 1停用）")
    private String status;

    /** 菜单图标 */
    @Schema(description = "菜单图标")
    private String icon;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 菜单类型（00系统菜单）03普通 */
    @Schema(description = "菜单类型（00系统菜单）03普通")
    private String sysMenuType;

    /**  */
    @Schema(description = "")
    private Boolean isDel;

}