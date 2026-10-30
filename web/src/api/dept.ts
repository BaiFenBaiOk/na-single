import { del, get, post, put } from './http/request'
import type { IdStr } from './types/common'
import type { DeptNode, DeptSavePayload } from './types/dept'

/** 部门树（返回全量树，不套分页适配） */
export function getDeptTree() {
  return get<DeptNode[]>('/api/dept/tree')
}

/** 新建部门 */
export function saveDept(body: DeptSavePayload) {
  return post<null>('/api/dept/save', body)
}

/** 编辑部门 */
export function updateDept(body: DeptSavePayload & { id: IdStr }) {
  return put<null>('/api/dept/update', body)
}

/** 删除部门（有子部门或用户绑定会被后端拒绝并返回提示） */
export function removeDept(id: IdStr) {
  return del<null>(`/api/dept/delete/${encodeURIComponent(id)}`)
}
