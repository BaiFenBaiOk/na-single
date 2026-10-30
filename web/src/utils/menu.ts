import type { MenuNode } from '@/api/types/menu'

/** 侧边栏外链菜单的 index 前缀，用于和内部路由 index 区分 */
export const EXTERNAL_MENU_PREFIX = 'external:'

/** 是否为站内路径：以 / 开头且不是 // 协议相对地址 */
export function isInternalPath(path?: string): boolean {
  const value = (path || '').trim()
  return value.startsWith('/') && !value.startsWith('//')
}

/**
 * 是否外链菜单。
 *
 * 以后端 is_frame 为准：0=是外链，1=否。
 * 兼容旧数据：is_frame 缺失时，path 以 http(s):// 开头的也按外链处理。
 */
export function isExternalMenu(node: Pick<MenuNode, 'isFrame' | 'path'>): boolean {
  const flag = String(node.isFrame ?? '')
  if (flag === '0') return true
  if (flag === '1') return false
  return /^https?:\/\//i.test((node.path || '').trim())
}

/**
 * 规范化外链地址。
 *
 * - 已写 http(s):// 的原样返回；
 * - `//host/path` 按当前站点协议补全；
 * - 以 `/` 开头的地址补当前站点 origin，避免新标签打开相对路径后 404；
 * - 其它缺协议的地址（如 `192.168.0.43:10061/xxx`）按当前站点协议补全，
 *   避免内网 http 环境被强制补成 https 后打不开。
 */
export function resolveExternalUrl(path: string): string {
  const value = (path || '').trim()
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
  if (value.startsWith('//')) return `${protocol}${value}`
  if (value.startsWith('/')) return `${window.location.origin}${value}`
  return `${protocol}//${value}`
}

/** 外链菜单在 el-menu 中的 index */
export function externalMenuIndex(path: string): string {
  return EXTERNAL_MENU_PREFIX + resolveExternalUrl(path)
}
