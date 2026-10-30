import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import { getUserTree } from '@/api/menu'
import type { MenuNode } from '@/api/types/menu'
import { transformMenusToRoutes } from '@/router/dynamic'
import { collectPerms, findFirstNavigablePath } from '@/utils/tree'
import { useMenuButtonStore } from './menuButton'
import { useUserStore } from './user'

interface PermissionState {
  /** 后端菜单原始树（侧边栏渲染源） */
  menus: MenuNode[]
  /** 由菜单树转换得到的动态路由 */
  dynamicRoutes: RouteRecordRaw[]
  /** 动态路由是否已生成并挂载 */
  loaded: boolean
}

/** 权限 / 动态路由状态 */
export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    menus: [],
    dynamicRoutes: [],
    loaded: false
  }),
  getters: {
    /** 登录后默认落地的第一个可导航页面 */
    firstPath(): string {
      return findFirstNavigablePath(this.menus) || '/profile'
    }
  },
  actions: {
    /**
     * 拉取菜单树 + 用户资料，聚合 perms，生成动态路由（不在此 addRoute，
     * 路由注册交由守卫调用 mountDynamicRoutes，规避 store ↔ router 循环依赖）。
     * @returns ok=false 表示无菜单（后端 -1 业务提示已由拦截器弹出），应引导 /403
     */
    async generate(): Promise<{ ok: boolean; message?: string }> {
      const userStore = useUserStore()
      try {
        const tree = await getUserTree()
        const list = Array.isArray(tree) ? tree : []
        if (list.length === 0) {
          this.menus = []
          this.dynamicRoutes = []
          this.loaded = false
          return { ok: false, message: '当前账号未分配任何菜单' }
        }
        this.menus = list
        userStore.setPerms(collectPerms(list))
        // 用户资料失败不阻塞菜单与路由生成
        try {
          await userStore.fetchProfile()
        } catch {
          // 忽略资料接口异常
        }
        this.dynamicRoutes = transformMenusToRoutes(list)
        this.loaded = true
        return { ok: true }
      } catch (e) {
        const code = (e as { code?: number })?.code
        if (code === 401) throw e // 交给守卫登出
        this.loaded = false
        return { ok: false, message: (e as Error)?.message }
      }
    },
    reset() {
      this.menus = []
      this.dynamicRoutes = []
      this.loaded = false
      // 按钮缓存按用户/角色变化，登出或重新登录时必须清空
      useMenuButtonStore().clear()
    }
  }
})
