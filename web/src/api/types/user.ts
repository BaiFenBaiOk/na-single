// 用户相关类型
import type { IdStr, PageQuery } from './common'

/** 用户分页列表行（对应 ResSysUserPagingVO） */
export interface UserRecord {
  id: IdStr
  userName: string
  /** 后端会回显明文密码，列表页严禁渲染该字段 */
  passWord?: string
  nickName: string
  /** 00 系统用户 / 03 普通 */
  userType: string
  email: string
  phone: string
  avatar: string
  /** 性别：0 未知 / 1 男 / 2 女 */
  sex: number
  /** 账号状态：Boolean 承载 0/1——false 正常 / true 停用 */
  status: boolean
  loginIp?: string
  loginTime?: string
  createTime?: string
  updateTime?: string
  remark?: string
  isDel?: boolean
  /** 在线令牌列表（非空表示该用户当前在线） */
  tokens?: unknown[]
  /** 已绑定角色 */
  roles?: UserRole[]
}

export interface UserRole {
  id: IdStr
  roleName: string
  roleCode: string
}

/** 用户分页查询参数（实际服务端只使用部分字段） */
export interface UserPageQuery extends PageQuery {
  userName?: string
  nickName?: string
  /** 账号状态：false 正常 / true 停用；空串表示不筛选 */
  status?: boolean | string
  isDel?: boolean
}

/** 新建 / 编辑用户提交体（id 为字符串长整数，新增由前端 generateId 兜底） */
export interface UserSavePayload {
  id: IdStr
  userName: string
  passWord?: string
  nickName: string
  userType: string
  email?: string
  phone?: string
  avatar?: string
  sex?: number
  /** 账号状态：false 正常 / true 停用 */
  status?: boolean
  remark?: string
}

/** 当前登录用户信息（对应 ResUserInfoVO） */
export interface UserInfo {
  id: IdStr
  userName: string
  nickName: string
  email: string
  phone: string
  createTime: string
  /** 账号状态：Boolean 承载 0/1——false=正常、true=停用（与列表接口的 Integer 不同） */
  status: boolean
  userType: string
  roleNames: string[]
  roleIds: IdStr[]
  deptName: string[]
  postName: string[]
}

/** 绑定角色请求（空数组=清空全部角色） */
export interface BindRolePayload {
  userId: IdStr
  roleIds: IdStr[]
}
