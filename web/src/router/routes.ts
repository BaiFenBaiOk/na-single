import type { RouteRecordRaw } from 'vue-router'

/**
 * 固定路由（无需菜单、无需动态生成）。
 * 登录后的 Layout 父路由（含动态叶子 + profile/monitor）由 mountDynamicRoutes 注册，
 * 故这里只放全屏页。
 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限', hidden: true }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', hidden: true }
  }
]

/** 登录后固定挂在 LayoutRoot 下的隐藏页面 */
export const fixedLayoutRoutes: RouteRecordRaw[] = [
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/profile/index.vue'),
    meta: { title: '个人中心', hidden: true }
  },
  {
    path: '/monitor',
    name: 'monitor',
    component: () => import('@/views/monitor/index.vue'),
    meta: { title: '系统监控', hidden: true }
  }
]
