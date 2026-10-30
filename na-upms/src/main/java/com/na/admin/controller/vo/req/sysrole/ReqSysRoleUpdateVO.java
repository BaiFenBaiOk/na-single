package com.na.admin.controller.vo.req.sysrole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Schema(description = "SysRole修改入参")
@Data
public class ReqSysRoleUpdateVO {

    /**  */
    @NotNull(message = "ID不能为空")
    @Schema(description = "")
    private Long id;

    /** 角色名称 */
    @Schema(description = "角色名称")
    private String roleName;

    /** 角色编码 */
    @Schema(description = "角色编码")
    private String roleCode;

    /** 显示顺序 */
    @Schema(description = "显示顺序")
    private Integer roleSort;

    /** 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限） */
    @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
    private String dataScope;

    /** 角色状态（false正常 true停用，对应数据库 0/1） */
    @Schema(description = "角色状态（false正常 true停用，对应数据库 0/1）")
    private Boolean status;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /** 角色类型（00系统角色）03普通 */
    @Schema(description = "角色类型（00系统角色）03普通")
    private String roleType;

    /**  */
    @Schema(description = "")
    private Boolean isDel;

}