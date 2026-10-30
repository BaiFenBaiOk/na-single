import { get, post } from './http/request'
import type { LoginParams, LoginResult } from './types/auth'

/** 登录（匿名接口） */
export function login(data: LoginParams) {
  return post<LoginResult>('/api/login', data)
}

/** 登出（清除服务端登录缓存） */
export function logout() {
  return get<null>('/api/login/loginOut')
}
