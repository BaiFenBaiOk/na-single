package com.na.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * sys_user业务模型
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysUserModel {

    /**  */
    private Long id;

    /** 用户账号 */
    private String userName;

    /** 密码 */
    private String passWord;

    /** 用户昵称 */
    private String nickName;

    /** 用户类型（00 系统内置 / 03 普通） */
    private String userType;

    /** 邮箱地址 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像 */
    private String avatar;

    /** 0默认  1男  2女 */
    private Integer sex;

    /** 帐号状态（0正常 1停用） */
    private Boolean status;

    /** 登陆IP */
    private String loginIp;

    /** 登录时间 */
    private LocalDateTime loginTime;

    /** 创建人 */
    private Long creatorId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /**  */
    private Boolean isDel;

}