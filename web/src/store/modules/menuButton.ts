import { defineStore } from 'pinia'
import { getMenuBtn } from '@/api/menu'
import type { MenuBtn } from '@/api/types/menu'

/** 失败负缓存时长：避免接口异常时每行组件重复请求 */
const FAIL_TTL = 10_000

/** menuId 归一化：空值 / 字符串化的 undefined、null 直接忽略 */
function normalizeMenuId(menuId?: string | number | null): string {
  if (menuId === undefined || menuId === null) return ''
  const id = String(menuId).trim()
  if (!id || id === 'undefined' || id === 'null') return ''
  return id
}

// Promise 不放进 Pinia state，避免被响应式代理；epoch 用于登出竞态保护
const pending = new Map<string, Promise<MenuBtn[]>>()
const failedAt = new Map<string, number>()
let epoch = 0

interface MenuButtonState {
  /** menuId -> 按钮列表 */
  cache: Record<string, MenuBtn[]>
  /** 是否已完成一次成功加载 */
  loaded: Record<string, boolean>
  /** 最近一次请求是否失败 */
  failed: Record<string, boolean>
}

/**
 * 列表页按钮缓存。
 * 同一 menuId 在首屏内只发一次请求，翻页 / 多行实例均复用缓存。
 */
export const useMenuButtonStore = defineStore('menuButton', {
  state: (): MenuButtonState => ({
    cache: {},
    loaded: {},
    failed: {}
  }),
  actions: {
    async ensure(menuIdInput?: string | number | null): Promise<MenuBtn[]> {
      const menuId = normalizeMenuId(menuIdInput)
      if (!menuId) return []

      // 已成功加载（含合法的空数组）
      if (this.loaded[menuId] && !this.failed[menuId]) {
        return this.cache[menuId] || []
      }
      // 失败负缓存：TTL 内不重复请求
      if (this.failed[menuId]) {
        const at = failedAt.get(menuId) || 0
        if (Date.now() - at < FAIL_TTL) {
          return this.cache[menuId] || []
        }
      }
      const inflight = pending.get(menuId)
      if (inflight) return inflight

      const requestEpoch = epoch
      let task: Promise<MenuBtn[]>
      task = getMenuBtn(menuId, { silent: true })
        .then((list) => {
          const data = Array.isArray(list) ? list : []
          // 登出后旧请求不回填缓存
          if (requestEpoch === epoch) {
            this.cache[menuId] = data
            this.loaded[menuId] = true
            this.failed[menuId] = false
            failedAt.delete(menuId)
          }
          return data
        })
        .catch(() => {
          if (requestEpoch === epoch) {
            this.cache[menuId] = []
            this.loaded[menuId] = true
            this.failed[menuId] = true
            failedAt.set(menuId, Date.now())
          }
          return []
        })
        .finally(() => {
          // 只清理自己注册的 Promise，避免旧请求删掉新请求
          if (pending.get(menuId) === task) {
            pending.delete(menuId)
          }
        })

      pending.set(menuId, task)
      return task
    },
    /** 强制重新拉取（后续页面可提供手动刷新入口） */
    reload(menuIdInput?: string | number | null): Promise<MenuBtn[]> {
      const menuId = normalizeMenuId(menuIdInput)
      if (!menuId) return Promise.resolve([])
      this.loaded[menuId] = false
      this.failed[menuId] = false
      failedAt.delete(menuId)
      // 丢弃在途请求，保证 reload 会真正发起一次新请求
      pending.delete(menuId)
      return this.ensure(menuId)
    },
    list(menuIdInput?: string | number | null): MenuBtn[] {
      const menuId = normalizeMenuId(menuIdInput)
      return menuId ? this.cache[menuId] || [] : []
    },
    /** 当前菜单下是否返回了指定权限标识的按钮 */
    hasPerm(menuIdInput: string | number | null | undefined, perms: string): boolean {
      if (!perms) return false
      return this.list(menuIdInput).some((item) => item.perms === perms)
    },
    /** 是否已完成加载（用于状态开关避免“先可点后禁用”） */
    isReady(menuIdInput?: string | number | null): boolean {
      const menuId = normalizeMenuId(menuIdInput)
      return !!menuId && !!this.loaded[menuId]
    },
    /** 登出 / 重新登录时清空；epoch 递增使在途请求失效 */
    clear() {
      epoch += 1
      pending.clear()
      failedAt.clear()
      this.cache = {}
      this.loaded = {}
      this.failed = {}
    }
  }
})
