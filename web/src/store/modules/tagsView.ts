import { defineStore } from 'pinia'
import type { RouteLocationNormalizedLoaded } from 'vue-router'

export interface TagView {
  /** 路由 path（共用模式下用于去重） */
  path: string
  /** 完整路径（含 query） */
  fullPath: string
  /** 路由 name（keep-alive include 使用） */
  name: string
  title: string
  icon?: string
  affix?: boolean
  query?: Record<string, unknown>
}

interface TagsViewState {
  visitedViews: TagView[]
  cachedViews: string[]
  /** 刷新计数器：变化时重建当前页面组件 */
  refreshKey: number
}

const AFFIX_PATHS = new Set(['/dashboard'])

/** 标签历史在 localStorage 中的 key（受主题设置「保留 TagsView 历史」控制） */
const HISTORY_KEY = 'na-tags-view-history'

/** 标签页状态（不持久化，刷新后由当前路由重新建立） */
export const useTagsViewStore = defineStore('tagsView', {
  state: (): TagsViewState => ({
    visitedViews: [],
    cachedViews: [],
    refreshKey: 0
  }),
  actions: {
    addView(route: RouteLocationNormalizedLoaded, share: boolean, cache: boolean) {
      if (!route.name || !route.path) return
      const name = String(route.name)
      const title = String(route.meta?.title || route.name || route.path)
      const icon = route.meta?.icon ? String(route.meta.icon) : ''
      const affix = AFFIX_PATHS.has(route.path) || route.meta?.affix === true

      const exists = this.visitedViews.find((view) =>
        share ? view.path === route.path : view.fullPath === route.fullPath
      )
      if (exists) {
        exists.fullPath = route.fullPath
        exists.query = { ...route.query }
        exists.title = title
        exists.icon = icon
        exists.name = name
      } else {
        this.visitedViews.push({
          path: route.path,
          fullPath: route.fullPath,
          name,
          title,
          icon,
          affix,
          query: { ...route.query }
        })
      }

      const cacheEnabled = cache && route.meta?.keepAlive === true
      if (cacheEnabled && !this.cachedViews.includes(name)) {
        this.cachedViews.push(name)
      }
    },

    delView(tag: TagView, share: boolean) {
      this.visitedViews = this.visitedViews.filter((view) =>
        share ? view.path !== tag.path : view.fullPath !== tag.fullPath
      )
      this.removeCache(tag.name)
    },

    delOthers(tag: TagView, share: boolean) {
      this.visitedViews = this.visitedViews.filter((view) => {
        if (view.affix) return true
        return share ? view.path === tag.path : view.fullPath === tag.fullPath
      })
      this.cachedViews = this.visitedViews
        .filter((view) => view.name)
        .map((view) => view.name)
    },

    delAll() {
      this.visitedViews = this.visitedViews.filter((view) => view.affix)
      this.cachedViews = this.visitedViews.map((view) => view.name)
    },

    /** 刷新：递增 key，让当前路由组件重新创建 */
    refreshView() {
      this.refreshKey++
    },

    sortViews(from: number, to: number) {
      if (from === to || from < 0 || to < 0) return
      const views = [...this.visitedViews]
      const [moved] = views.splice(from, 1)
      if (!moved) return
      views.splice(to, 0, moved)
      this.visitedViews = views
    },

    removeCache(name: string) {
      this.cachedViews = this.cachedViews.filter((item) => item !== name)
    },

    /** 从本地恢复上次的标签历史 */
    restoreHistory() {
      try {
        const raw = localStorage.getItem(HISTORY_KEY)
        if (!raw) return
        const list = JSON.parse(raw) as TagView[]
        if (Array.isArray(list)) {
          this.visitedViews = list.filter((item) => item && item.path && item.name)
        }
      } catch {
        // 历史数据异常时忽略
      }
    },

    /** 保存 / 清除标签历史（enabled=false 时移除本地记录） */
    persistHistory(enabled: boolean) {
      try {
        if (enabled) {
          localStorage.setItem(HISTORY_KEY, JSON.stringify(this.visitedViews))
        } else {
          localStorage.removeItem(HISTORY_KEY)
        }
      } catch {
        // 本地存储不可用时忽略
      }
    }
  }
})
