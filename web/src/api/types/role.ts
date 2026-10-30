// 角色相关类型
import type { IdStr, PageQuery } from './common'

/** 角色行（对应 ResSysRolePagingVO） */
export interface RoleRecord {
  id: IdStr
  roleName: string
  roleCode: string
  /** 排序 */
  roleSort?: number
  /** 数据范围：1 全部 / 2 自定义 / 3 本部门 / 4 本部门及以下。1 仅系统用户可设 */
  dataScope: string
  /** 状态：Boolean 承载 0/1——false 正常 / true 停用 */
  status: boolean
  createTime?: string
  updateTime?: string
  /** 00 系统角色 / 03 普通 */
  roleType: string
  isDel?: boolean
}

export interface RolePageQuery extends PageQuery {
  isDel?: boolean
  roleName?: string
  /** false 正常 / true 停用；空串表示不筛选 */
  status?: boolean | string
}

/** 新建 / 编辑角色提交体 */
export interface RoleSavePayload {
  id: IdStr
  roleName: string
  roleCode: string
  roleSort?: number
  dataScope: string
  /** false 正常 / true 停用 */
  status?: boolean
  roleType: string
  isDel?: boolean
}
