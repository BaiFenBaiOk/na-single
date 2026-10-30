import { defineAsyncComponent, defineComponent, h } from 'vue'
import type { RouteComponent, RouteRecordRaw } from 'vue-router'
import type { MenuNode } from '@/api/types/menu'
import { flattenTree, normalizePath, sortMenuTree } from '@/utils/tree'
import { isExternalMenu, isInternalPath } from '@/utils/menu'

/**
 * 菜单树 → 动态路由。
 *
 * 设计：路由层级与菜单视觉层级解耦——所有可导航叶子平铺到同一个 Layout 下，
 * 路径直接取后端节点的绝对 path（如 /userList）。这样：
 *  1) 避开多级 ParentView 下 path 拼接与后端单级 path 不一致导致的 404；
 *  2) 侧边栏仍按原始树渲染多级，点击直接 push 节点绝对 path，二者天然一致。
 *
 * 兜底已覆盖的种子异常：M「首页」自带 component 且无子节点；C 套 C；最深 4 层。
 */

// 仅 00 系统用户可见的入口 name（登录树不回填 sysMenuType，用种子 name 兜底）
const SYS_DEF_ROUTE_NAMES = new Set(['menuList'])

// 视图组件：用 glob 按 component 字符串匹配
const viewModules = import.meta.glob('../views/**/*.vue')

const Layout: RouteComponent = () => import('@/layout/index.vue')
const Placeholder: RouteComponent = () => import('@/views/error/Placeholder.vue')

// 特例别名（种子中首页组件名为大写 /Dashboard）
const VIEW_ALIAS: Record<string, RouteComponent> = {
  '/Dashboard': () => import('@/views/dashboard/index.vue')
}

function resolveView(component?: string): RouteComponent {
  const c = (component || '').trim()
  if (!c) return Placeholder
  if (VIEW_ALIAS[c]) return VIEW_ALIAS[c]
  const suffix = c.replace(/^\/+/, '') // "/user/list" → "user/list"
  const hitKey =
    Object.keys(viewModules).find((k) => k.endsWith(`views/${suffix}.vue`)) ||
    Object.keys(viewModules).find((k) => k.endsWith(`views/${suffix}/index.vue`))
  if (hitKey) {
    return viewModules[hitKey] as RouteComponent
  }
  // 未匹配时不白屏，落到占位页并告警，便于首次联调核对菜单 component
  console.warn('[menu] 未匹配到组件，使用占位页：', component)
  return Placeholder
}

function buildMeta(node: MenuNode) {
  return {
    title: node.menuName,
    icon: node.icon,
    menuId: String(node.id),
    perms: node.perms || '',
    keepAlive: Number(node.isCache) === 0,
    hidden: node.visible === '1',
    requireSysDef: SYS_DEF_ROUTE_NAMES.has(node.name),
    // 原样带上菜单里配的组件路径，仅用于落占位页时提示排查
    rawComponent: node.component || ''
  }
}

function isNavigableLeaf(node: MenuNode): boolean {
  if (node.status === '1') return false
  // 真正的外部地址（http(s):// 或 //host）不建内部路由；
  // is_frame=0 但 path 是站内路径时保留路由，保证新标签打开本系统页面能命中组件
  if (isExternalMenu(node) && !isInternalPath(node.path)) return false
  // C 菜单一律是叶子（即便挂了 C 子节点，视觉上仍可点开自身，主路径用自身 component）
  if (node.menuType === 'C') return true
  // M 目录：仅当自带 component 且没有可路由子节点时才作为叶子（首页特例）
  const hasRoutableChild = (node.children || []).some(
    (c) => (c.menuType === 'M' || c.menuType === 'C') && c.status !== '1'
  )
  if (node.menuType === 'M' && node.component && !hasRoutableChild) return true
  return false
}

/** 由菜单树生成动态路由（单个 Layout 父路由 + 全部叶子 children） */
export function transformMenusToRoutes(rawMenus: MenuNode[]): RouteRecordRaw[] {
  const menus = sortMenuTree(rawMenus)
  const all = flattenTree(menus)

  const leafRoutes: RouteRecordRaw[] = []
  const usedNames = new Set<string>()

  all
    .filter(isNavigableLeaf)
    .forEach((node) => {
      const fullPath = normalizePath(node.path)
      if (!fullPath) return
      // 极端情况下后端 name 缺失，用 path 兜底保证 name 唯一（keep-alive 依赖）
      let name = node.name || fullPath.replace(/^\//, '')
      while (usedNames.has(name)) name = `${name}_x`
      usedNames.add(name)
      // 包装一层同名组件：keep-alive 的 include 按组件 name 匹配路由 name
      const AsyncView = defineAsyncComponent(resolveView(node.component) as any)
      const namedView = defineComponent({
        name,
        setup() {
          return () => h(AsyncView)
        }
      })
      leafRoutes.push({
        path: fullPath,
        name,
        component: namedView,
        meta: buildMeta(node)
      })
    })

  const layoutRoute: RouteRecordRaw = {
    path: '/',
    name: 'LayoutRoot',
    component: Layout,
    redirect: '/dashboard',
    children: leafRoutes
  }
  return [layoutRoute]
}
