package com.na.admin.controller.vo.req.sysuser;

import com.na.common.base.BasePagesParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Schema(description = "SysUser分页查询入参")
@Data
public class ReqSysUserPagingVO extends BasePagesParam {

    @Schema(description = "")
    private Long id;

    /** 用户账号 */
    @Schema(description = "用户账号")
    private String userName;

    /** 密码 */
    @Schema(description = "密码")
    private String passWord;

    /** 用户昵称 */
    @Schema(description = "用户昵称")
    private String nickName;

    /** 用户类型（00系统用户) 03普通 */
    @Schema(description = "用户类型（00系统用户) 03普通")
    private String userType;

    /** 邮箱地址 */
    @Schema(description = "邮箱地址")
    private String email;

    /** 手机号 */
    @Schema(description = "手机号")
    private String phone;

    /** 头像 */
    @Schema(description = "头像")
    private String avatar;

    /** 0默认  1男  2女 */
    @Schema(description = "0默认  1男  2女")
    private Integer sex;

    /** 帐号状态（false正常 true停用，对应数据库 0/1） */
    @Schema(description = "帐号状态（false正常 true停用，对应数据库 0/1）")
    private Boolean status;

    /** 登陆IP */
    @Schema(description = "登陆IP")
    private String loginIp;

    /** 登录时间 */
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    /** 创建人 */
    @Schema(description = "创建人")
    private Long creatorId;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "")
    private Boolean isDel;
}
