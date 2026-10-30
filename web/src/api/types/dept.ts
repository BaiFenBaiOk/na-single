// 部门相关类型
import type { IdStr } from './common'

/** 部门树节点（对应 ResSysDeptTreeVO；接口返回全量树，非分页壳） */
export interface DeptNode {
  id: IdStr
  /** 上级部门ID（'0' 为顶级部门） */
  parentId: IdStr
  name: string
  orderNum?: number
  remark?: string
  createTime?: string
  updateTime?: string
  isDel?: boolean
  /** 树层级（组树时回填，根为 1） */
  level?: number
  children?: DeptNode[]
}

/** 新建 / 编辑部门提交体 */
export interface DeptSavePayload {
  /** 上级部门ID，0 或缺省为顶级部门 */
  parentId?: IdStr
  name: string
  orderNum?: number
  remark?: string
}
