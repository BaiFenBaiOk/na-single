// 登录认证相关类型
import type { IdStr } from './common'

/** 登录请求体（对应后端 ReqLoginVO，四项均必填） */
export interface LoginParams {
  /** 用户名（后端自动 trim + toLowerCase） */
  userName: string
  /** 密码（后端明文比对，前端不哈希） */
  passWord: string
  /** 验证码标识，取验证码接口路径中的 uuid */
  uuid: string
  /** 用户输入的图形验证码 */
  captcha: string
}

/** 登录成功响应（对应后端 ResLoginVO） */
export interface LoginResult {
  userId: IdStr
  userName: string
  nickName: string
  /** 用户类型码：00 系统用户 / 03 普通 */
  userType: string
  /** 登录令牌，后续请求放入 Authorization: Bearer <token> */
  token: string
  loginTime: string
  loginIp: string
  /** 登录归属地，后端为 List<String> */
  loginAddress: string[]
  email: string
  phone: string
  avatar: string
  deptName: string
  /** 后端恒为 null，前端不要使用（接口级 URI 鉴权由服务端过滤器处理） */
  requestUriSet: string[] | null
}
