import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import { getUserInfo } from '@/api/user'
import type { LoginParams, LoginResult } from '@/api/types/auth'
import type { UserInfo } from '@/api/types/user'
import { SYS_USER_TYPE } from '@/constants/businessCode'
import { wsClient } from '@/utils/websocket'
import { useAppStore } from './app'

interface UserState {
  /** JWT 令牌（持久化，刷新免登录） */
  token: string
  /** 登录返回的主体信息（持久化用于首屏展示，刷新后以 userInfo 重新校准） */
  loginInfo: LoginResult | null
  /** 当前用户详情 */
  userInfo: UserInfo | null
  /** 聚合后的权限标识集合（来自整棵菜单树） */
  perms: string[]
  /** 当前用户角色 id */
  roleIds: string[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: '',
    loginInfo: null,
    userInfo: null,
    perms: [],
    roleIds: []
  }),
  getters: {
    /** 是否系统用户（平台方，仅认 00） */
    isSysUser(): boolean {
      return this.loginInfo?.userType === SYS_USER_TYPE.SYS_DEF
    },
    /** 展示名（优先昵称） */
    displayName(): string {
      return this.userInfo?.nickName || this.loginInfo?.nickName || this.loginInfo?.userName || ''
    },
    avatar(): string {
      return this.loginInfo?.avatar || ''
    },
    hasPerm: (state) => (perm: string) => state.perms.includes(perm)
  },
  actions: {
    /** 登录：只负责拿 token 与主体信息，菜单由路由守卫加载 */
    async login(form: LoginParams) {
      const data = await authApi.login(form)
      this.token = data.token
      this.loginInfo = data
      // 登录成功后解除可能残留的锁屏状态
      useAppStore().unlockScreen()
      // 登录成功后建立 WebSocket 连接（失败不阻塞登录流程）
      wsClient.connect()
      return data
    },
    /** 拉取当前用户详情（刷新后用于重建上下文） */
    async fetchProfile() {
      const info = await getUserInfo()
      this.userInfo = info
      this.roleIds = info.roleIds || []
      // 登录返回里没有的归属信息（如 userType）以详情为准做兜底
      if (info && this.loginInfo) {
        this.loginInfo = { ...this.loginInfo, userType: info.userType, nickName: info.nickName }
      }
      return info
    },
    setPerms(perms: string[]) {
      this.perms = perms
    },
    /** 登出：尽力调服务端清理缓存，再清本地态（动态路由由守卫/重置处理） */
    async logout(callApi = true) {
      if (callApi && this.token) {
        try {
          await authApi.logout()
        } catch {
          // 服务端清理失败不阻塞本地登出
        }
      }
      this.resetLocal()
    },
    resetLocal() {
      // 主动断开 WebSocket，避免登出后仍保持长连接
      wsClient.disconnect(1000, 'logout')
      this.token = ''
      this.loginInfo = null
      this.userInfo = null
      this.perms = []
      this.roleIds = []
      // 登出/会话失效时清除锁屏状态，避免下次登录后直接落到锁屏
      useAppStore().unlockScreen()
    }
  },
  persist: {
    key: 'na-user',
    // 只持久化 token 与登录主体；perms/菜单刷新后由守卫重拉，避免陈旧
    pick: ['token', 'loginInfo']
  }
})
