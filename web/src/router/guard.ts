import NProgress from 'nprogress'
import router, { mountDynamicRoutes, resetRouter } from './index'
import { usePermissionStore } from '@/store/modules/permission'
import { useUserStore } from '@/store/modules/user'

NProgress.configure({ showSpinner: false })

/** 无需登录即可访问的白名单 */
const WHITE_LIST = ['/login', '/403', '/404']

/** 安装全局路由守卫 */
export function setupRouterGuard() {
  router.beforeEach(async (to) => {
    NProgress.start()
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    // 登录页：已登录则直接进首页
    if (to.path === '/login') {
      return userStore.token ? '/' : true
    }
    if (WHITE_LIST.includes(to.path)) {
      return true
    }
    // 未登录
    if (!userStore.token) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    // 已登录但动态路由尚未生成
    if (!permissionStore.loaded) {
      try {
        const result = await permissionStore.generate()
        if (!result.ok) {
          // 无菜单权限：后端业务提示已由请求拦截器弹出，这里引导 403（不踢登录，避免死循环）
          return { path: '/403' }
        }
        mountDynamicRoutes(permissionStore.dynamicRoutes)
        // 重新进入，让新路由表参与匹配
        return { path: to.fullPath, replace: true }
      } catch (e) {
        const code = (e as { code?: number })?.code
        if (code === 401) {
          await userStore.logout(false)
          resetRouter()
          return { path: '/login', query: { redirect: to.fullPath } }
        }
        return { path: '/403' }
      }
    }

    // 系统用户专属入口兜底
    if (to.meta.requireSysDef && !userStore.isSysUser) {
      return { path: '/403' }
    }
    return true
  })

  router.afterEach((to) => {
    NProgress.done()
    const baseTitle = import.meta.env.VITE_APP_TITLE || '管理平台'
    document.title = to.meta.title ? `${to.meta.title} - ${baseTitle}` : baseTitle
  })
}
