import { del, get, post, put } from './http/request'
import { normalizePage } from './http/pageAdapter'
import type { RawPage, IdStr, PageResult } from './types/common'
import type {
  BindRolePayload,
  UserInfo,
  UserPageQuery,
  UserRecord,
  UserSavePayload
} from './types/user'

/** 用户分页（响应经分页错位适配） */
export async function userPage(body: UserPageQuery): Promise<PageResult<UserRecord>> {
  const raw = await post<RawPage<UserRecord>>('/api/sysuser/list-paging', body)
  return normalizePage(raw)
}

/** 新建用户 */
export function saveUser(body: UserSavePayload) {
  return post<null>('/api/sysuser/save', body)
}

/** 编辑用户 */
export function updateUser(body: Partial<UserSavePayload> & { id: IdStr }) {
  return put<null>('/api/sysuser/update', body)
}

/** 删除用户（00 系统用户不可删；仍有角色绑定时后端拒绝） */
export function removeUser(id: IdStr) {
  return del<null>(`/api/sysuser/delete/${encodeURIComponent(id)}`)
}

/** 当前登录用户信息 */
export function getUserInfo() {
  return get<UserInfo>('/api/sysuser/userInfo')
}

/** 锁屏解锁：校验当前登录用户的登录密码 */
export function verifyPassword(passWord: string) {
  return post<null>('/api/sysuser/verifyPassword', { passWord }, { silent: true })
}

/** 查询某用户已绑定的角色 id 集合 */
export function getBindRoles(userId: IdStr) {
  return get<IdStr[]>(`/api/sysuser/getBindRoleByUserId/${userId}`)
}

/** 绑定角色（空数组=清空全部角色） */
export function bindRoles(body: BindRolePayload) {
  return put<null>('/api/sysuser/bindRole', body)
}

/** 查询某用户已绑定的部门 id（无绑定返回 null） */
export function getBindDept(userId: IdStr) {
  return get<IdStr | null>(`/api/sysuser/getBindDept/${userId}`)
}

/** 绑定部门（deptId 传空/不传 = 解除绑定） */
export function bindDept(body: { userId: IdStr; deptId?: IdStr | null }) {
  return put<null>('/api/sysuser/bindDept', body)
}

/** 查询某用户已绑定的岗位 id 集合 */
export function getBindPosts(userId: IdStr) {
  return get<IdStr[]>(`/api/sysuser/getBindPost/${userId}`)
}

/** 绑定岗位（空数组 = 清空全部岗位） */
export function bindPosts(body: { userId: IdStr; postIds: IdStr[] }) {
  return put<null>('/api/sysuser/bindPost', body)
}

/** 踢用户下线（清空其 token），路径参数为 userId */
export function kickUser(userId: IdStr) {
  return get<null>(`/api/sysuser/clear/${encodeURIComponent(userId)}`)
}
