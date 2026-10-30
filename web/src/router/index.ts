import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { constantRoutes, fixedLayoutRoutes } from './routes'

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

const LAYOUT_ROOT = 'LayoutRoot'
const CATCH_ALL = 'catch-all-404'
let dynamicNames: string[] = []

/**
 * 挂载动态路由：先注册 Layout 父路由（含全部菜单叶子），
 * 再把 profile/monitor 等固定登录后页面挂到 LayoutRoot，最后补 404 兜底。
 * 404 必须最后注册，否则刷新动态路径会先命中兜底。
 */
export function mountDynamicRoutes(routes: RouteRecordRaw[]) {
  routes.forEach((route) => {
    if (route.name) {
      router.addRoute(route)
      dynamicNames.push(String(route.name))
    }
  })
  fixedLayoutRoutes.forEach((route) => {
    router.addRoute(LAYOUT_ROOT, route)
  })
  if (!router.hasRoute(CATCH_ALL)) {
    router.addRoute({
      path: '/:pathMatch(.*)*',
      name: CATCH_ALL,
      redirect: '/404'
    })
  }
}

/** 重置：移除全部动态路由（登出 / 重新登录时调用） */
export function resetRouter() {
  const all = [...dynamicNames, ...fixedLayoutRoutes.map((r) => String(r.name)), CATCH_ALL]
  all.forEach((name) => {
    if (router.hasRoute(name)) router.removeRoute(name)
  })
  dynamicNames = []
}

export default router
