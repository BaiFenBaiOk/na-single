import { defineStore } from 'pinia'
import { DEFAULT_THEME, type ThemeConfig } from '@/utils/theme'

export type AppLanguage = 'zh-CN' | 'en'

interface AppState {
  /** PC 侧边栏折叠态（持久化） */
  sidebarCollapsed: boolean
  /** H5 抽屉显隐（不持久化） */
  drawerVisible: boolean
  /** 界面语言（持久化） */
  language: AppLanguage
  /** 是否锁屏（持久化，刷新后仍保持锁定） */
  locked: boolean
  /** 主题配置（持久化） */
  theme: ThemeConfig
  /** 主题设置抽屉（不持久化） */
  themeDrawerVisible: boolean
}

/** 全局应用 UI 状态 */
export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    sidebarCollapsed: false,
    drawerVisible: false,
    language: 'zh-CN',
    locked: false,
    theme: { ...DEFAULT_THEME },
    themeDrawerVisible: false
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setSidebar(collapsed: boolean) {
      this.sidebarCollapsed = collapsed
    },
    openDrawer() {
      this.drawerVisible = true
    },
    closeDrawer() {
      this.drawerVisible = false
    },
    setLanguage(language: AppLanguage) {
      this.language = language
    },
    lockScreen() {
      this.locked = true
    },
    unlockScreen() {
      this.locked = false
    },
    openThemeDrawer() {
      this.themeDrawerVisible = true
    },
    closeThemeDrawer() {
      this.themeDrawerVisible = false
    },
    resetTheme() {
      this.theme = { ...DEFAULT_THEME }
    }
  },
  persist: {
    key: 'na-app',
    pick: ['sidebarCollapsed', 'language', 'locked', 'theme']
  }
})
