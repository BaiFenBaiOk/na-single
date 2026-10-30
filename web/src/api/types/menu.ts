// 菜单 / 权限相关类型
import type { IdStr } from './common'

/** 列表页按钮（对应 sys_menu 中 menu_type='F' 的记录） */
export interface MenuBtn {
  id: IdStr
  /** 按钮名称（数据库 menu_name） */
  menuName: string
  /** 权限标识，页面 action 映射的 key */
  perms: string
  /** F 按钮为颜色语义：success / primary / danger / warning / info / # */
  icon: string
  parentId: IdStr | null
}

/** 菜单形态码（menu_type char(1)） */
export type MenuType = 'M' | 'C' | 'F' | 'A'

/**
 * 菜单树节点（对应后端 ResMenuTreeVO）。
 * 注意：F 按钮 / A 接口节点会混在 children 中（listBtn 恒为 null）；
 * 登录菜单树不回填 sysMenuType，只有菜单管理的 list-paging 才有。
 */
export interface MenuNode {
  id: IdStr
  /** 路由 name（全局唯一，keep-alive 依赖） */
  name: string
  /** 菜单名称 */
  menuName: string
  parentId: IdStr
  parentName?: string
  orderNum: number
  /** 路由地址 */
  path: string
  /** 组件路径，如 /user/list、/Dashboard */
  component: string
  /** 后端 API 地址（F/A 节点） */
  requestUri: string
  /** 权限标识，如 user:save:post */
  perms: string
  /** 路由参数 */
  query: string
  /** 是否外链：0 是 / 1 否 */
  isFrame: number | string
  /** 是否缓存：0 缓存 / 1 不缓存 */
  isCache: number | string
  menuType: MenuType | string
  /** 是否显示：0 显示 / 1 隐藏 */
  visible: string
  /** 状态：0 正常 / 1 停用 */
  status: string
  /** 图标：M/C 为 EP 图标名，F 为按钮类型(success/primary/danger/#) */
  icon: string
  remark?: string
  /** 归属级别：00 系统内置 / 03 普通（登录树可能不返回） */
  sysMenuType?: string
  /** 仅为补全树层级而加入的祖先节点（不在授权范围内），应渲染为不可勾选 */
  disabled?: boolean
  isDel?: boolean
  /** 树层级（后端装配） */
  level?: number
  children?: MenuNode[]
  /** 按菜单挂的按钮列表（登录树接口恒为 null，占位保留） */
  listBtn?: MenuNode[] | null
}

/** 角色菜单授权回显结构 */
export interface MenuAssignEcho {
  /** 完整菜单树（供 el-tree 渲染） */
  menuTree: MenuNode[]
  /** 已授权菜单 id 的扁平集合（回显优先使用） */
  menuIds?: IdStr[]
  /** 已授权菜单的「根→叶」完整 id 链集合（旧格式，两者都返回时优先用 menuIds） */
  menuIdsArr?: IdStr[][]
}
