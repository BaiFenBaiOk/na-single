import type { MenuNode } from '@/api/types/menu'
import type { IdStr } from '@/api/types/common'
import { isExternalMenu, isInternalPath } from '@/utils/menu'

/** 深度优先遍历树，返回扁平化数组 */
export function flattenTree<T extends { children?: T[] }>(list: T[] | undefined | null): T[] {
  const result: T[] = []
  const walk = (nodes?: T[] | null) => {
    nodes?.forEach((node) => {
      result.push(node)
      walk(node.children)
    })
  }
  walk(list)
  return result
}

/** 遍历菜单树（带回调） */
export function walkMenuTree(
  list: MenuNode[] | undefined | null,
  cb: (node: MenuNode) => void
): void {
  list?.forEach((node) => {
    cb(node)
    walkMenuTree(node.children, cb)
  })
}

/**
 * 整树收集权限标识 perms。
 * F 按钮 / A 接口节点混在 children 中（listBtn 恒为 null），凡 perms 非空即收集。
 */
export function collectPerms(list: MenuNode[] | undefined | null): string[] {
  const set = new Set<string>()
  walkMenuTree(list, (node) => {
    if (node.perms && node.perms.trim()) set.add(node.perms.trim())
  })
  return [...set]
}

/** 按 orderNum 稳定排序（null/undefined 排最后），返回新数组，不改原树 */
export function sortMenuTree(list: MenuNode[]): MenuNode[] {
  return [...list]
    .map((node) => {
      const cloned = { ...node }
      if (node.children && node.children.length) {
        cloned.children = sortMenuTree(node.children)
      } else {
        cloned.children = undefined
      }
      return cloned
    })
    .sort((a, b) => (a.orderNum ?? 9999) - (b.orderNum ?? 9999))
}

/**
 * 授权树回显：把后端给的「根→叶 id 链」转换为「应勾选的叶子 id」。
 * 规则：仅当某 id 在链集合中没有「同样在集合内的后代」时才作为勾选项，
 * 其余交给 el-tree 自动计算父级半选，避免整条祖先链被误判为全选。
 */
export function leafCheckedFromChains(chains: IdStr[][]): IdStr[] {
  const flat = new Set<IdStr>(chains.flat())
  // 构建「父 -> 子」关系：每条链上相邻节点
  const hasSelectedChild = new Set<IdStr>()
  chains.forEach((chain) => {
    for (let i = 0; i < chain.length - 1; i++) {
      if (flat.has(chain[i + 1])) hasSelectedChild.add(chain[i])
    }
  })
  return [...flat].filter((id) => !hasSelectedChild.has(id))
}

/**
 * 授权树回显：把「已授权 id 扁平集合」转换为「应勾选的末端节点」。
 * 规则与 leafCheckedFromChains 相同——只勾选后代中没有已授权项的节点，
 * 父级交给 el-tree 自动算半选；否则联动模式下会把未授权的子项一并勾上。
 */
export function leafCheckedFromIds(
  tree: MenuNode[] | undefined | null,
  ids: IdStr[]
): IdStr[] {
  const selected = new Set<IdStr>(ids)
  const hasSelectedDescendant = (node: MenuNode): boolean =>
    (node.children || []).some(
      (child) => selected.has(child.id) || hasSelectedDescendant(child)
    )
  return flattenTree(tree)
    .filter((node) => selected.has(node.id) && !hasSelectedDescendant(node))
    .map((node) => node.id)
}

/** 数组去重 */
export function uniq<T>(list: T[]): T[] {
  return [...new Set(list)]
}

/** 找到第一个可导航叶子的完整路径（M 目录拼接其首个 C 子孙的 path），用于 / 重定向 */
export function findFirstNavigablePath(list: MenuNode[]): string {
  for (const node of list) {
    // 真正的外部地址不能作为登录后的默认落地路由
    if (isExternalMenu(node) && !isInternalPath(node.path)) continue
    const path = normalizePath(node.path)
    if (node.menuType === 'C' && node.status !== '1') {
      return path
    }
    if (node.children?.length) {
      const childPath = findFirstNavigablePath(node.children)
      if (childPath) return childPath
    }
    // 形如「首页」的 M 自带 component：直接用自身 path
    if (node.component && node.status !== '1') {
      return path
    }
  }
  return ''
}

/** 过滤可在导航中展示的菜单（未隐藏、未停用的 M/C） */
export function visibleMenuChildren(children?: MenuNode[] | null): MenuNode[] {
  return (children || []).filter(
    (c) => c.visible !== '1' && c.status !== '1' && (c.menuType === 'M' || c.menuType === 'C')
  )
}

/** 在菜单树中查找 path 所属的顶级菜单 */
export function findTopMenu(list: MenuNode[], path: string): MenuNode | undefined {
  const target = normalizePath(path)
  const walk = (nodes: MenuNode[], top?: MenuNode): MenuNode | undefined => {
    for (const node of nodes) {
      const currentTop = top || node
      if (normalizePath(node.path) === target) return currentTop
      if (node.children?.length) {
        const hit = walk(node.children, currentTop)
        if (hit) return hit
      }
    }
    return undefined
  }
  return walk(list)
}

/** 去掉前导斜杠（子路由 path 用相对路径） */
export function trimSlash(p?: string): string {
  return (p || '').replace(/^\/+/, '')
}

/** 补前导斜杠（根级绝对 path） */
export function normalizePath(p?: string): string {
  if (!p) return ''
  return p.startsWith('/') ? p : `/${p}`
}
