import { del, post, put } from './http/request'
import { normalizePage } from './http/pageAdapter'
import type { RawPage, IdStr, PageResult } from './types/common'
import type { RolePageQuery, RoleRecord, RoleSavePayload } from './types/role'

/** 角色分页 */
export async function rolePage(body: RolePageQuery): Promise<PageResult<RoleRecord>> {
  const raw = await post<RawPage<RoleRecord>>('/api/sysrole/list-paging', body)
  return normalizePage(raw)
}

/** 新建角色（id 由前端 generateId 兜底以通过 @NotNull 校验，服务端雪花落库） */
export function saveRole(body: RoleSavePayload) {
  return post<null>('/api/sysrole/save', body)
}

/** 编辑角色 */
export function updateRole(body: Partial<RoleSavePayload> & { id: IdStr }) {
  return put<null>('/api/sysrole/update', body)
}

/** 删除角色（仍有用户绑定时会被后端拒绝；00 系统角色不可删） */
export function removeRole(id: IdStr) {
  return del<null>(`/api/sysrole/delete/${encodeURIComponent(id)}`)
}
