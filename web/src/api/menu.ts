import type { AxiosRequestConfig } from 'axios'
import { del, get, post,put } from './http/request'
import type { IdStr } from './types/common'
import type { MenuAssignEcho, MenuBtn, MenuNode } from './types/menu'

/** 当前登录用户的菜单树（动态路由 / 侧边栏数据源） */
export function getUserTree() {
  return get<MenuNode[]>('/api/sysuser/tree')
}

/** 某列表路由菜单下的按钮（menuId 为列表路由对应的 sys_menu.id） */
export function getMenuBtn(menuId: IdStr, config?: AxiosRequestConfig) {
  return get<MenuBtn[]>(`/api/sysuser/getMenuBtn/${menuId}`, undefined, config)
}

/**
 * 菜单管理：菜单树（名为分页，实际返回全量树，非分页壳）。
 * @param body 可传 menuName 关键字，服务端会补全祖先链
 */
export function menuTreePage(body?: Record<string, unknown>) {
  return post<MenuNode[]>('/api/sysmenu/list-paging', body ?? {})
}

/** 角色菜单授权回显：{ menuTree, menuIdsArr, menuIds } */
export function roleMenuEcho(body: { roleId: IdStr }) {
  return post<MenuAssignEcho>('/api/sysmenu/list-paging-role-format', body)
}

/** 保存角色菜单（menuIds 可空=清空该角色的菜单授权） */
export function saveRoleMenu(body: { roleId: IdStr; menuIds: IdStr[] }) {
  return post<null>('/api/sysmenu/save-role-menu', body)
}

/** 新建菜单（仅 00） */
export function saveMenu(body: Record<string, unknown>) {
  return post<null>('/api/sysmenu/save', body)
}

/** 编辑菜单（仅 00） */
export function updateMenu(body: Record<string, unknown>) {
  return put<null>('/api/sysmenu/update', body)
}

/** 删除菜单（仅 00；00 系统菜单不可删，有子菜单时后端拒绝） */
export function removeMenu(id: IdStr) {
  return del<null>(`/api/sysmenu/delete/${encodeURIComponent(id)}`)
}
