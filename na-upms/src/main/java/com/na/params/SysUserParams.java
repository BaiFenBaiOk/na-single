package com.na.params;

import com.na.common.base.BasePagesParam;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * sys_user查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysUserParams extends BasePagesParam {

    /**  */
    private Long id;

    /** 用户账号 */
    private String userName;

    /** 密码 */
    private String passWord;

    /** 用户昵称 */
    private String nickName;

    /** 用户类型（00系统用户) 03普通 */
    private String userType;

    /** 邮箱地址 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像 */
    private String avatar;

    /** 0默认  1男  2女 */
    private Integer sex;

    /** 帐号状态（false正常 true停用，对应数据库 0/1） */
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