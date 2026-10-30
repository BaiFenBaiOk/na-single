/**
 * 主题配置与运行时应用。
 *
 * 通过覆盖根节点 CSS 变量实现主题色、顶栏/菜单配色、深色模式、渐变等，
 * Element Plus 的主色变量同步更新；配置由 app store 持久化。
 */

export type LayoutType = 'sidebar' | 'classic' | 'transverse' | 'columns'
export type TagsStyle = 'tags-style-one' | 'tags-style-four' | 'tags-style-five'
export type PageAnimation = 'slide-right' | 'slide-left' | 'opacitys'
export type ColumnsAsideStyle = 'columns-round' | 'columns-card'
export type ColumnsAsideLayout = 'columns-vertical' | 'columns-horizontal'

export interface ThemeConfig {
  /** 布局：左侧 / 左顶（经典）/ 横向 / 分栏 */
  layout: LayoutType

  // ==================== 全局主题 ====================
  /** 品牌主色 */
  primaryColor: string
  /** 深色模式 */
  darkMode: boolean
  /** Element Plus 组件大小 */
  componentSize: 'small' | 'default' | 'large'

  // ==================== 顶栏设置 ====================
  /** 顶栏背景色；空字符串表示跟随默认主题 */
  headerBg: string
  /** 顶栏字体颜色；空字符串表示跟随默认主题 */
  headerTextColor: string
  /** 顶栏背景渐变 */
  headerBgGradual: boolean

  // ==================== 菜单设置 ====================
  /** 侧边菜单背景色 */
  menuBg: string
  /** 菜单字体颜色 */
  menuTextColor: string
  /** 菜单选中字体颜色 */
  menuActiveTextColor: string
  /** 菜单选中背景色 */
  menuActiveBg: string
  /** 菜单背景渐变 */
  menuBgGradual: boolean

  // ==================== 分栏设置 ====================
  /** 分栏一级菜单背景色 */
  columnsMenuBg: string
  /** 分栏一级菜单字体颜色 */
  columnsMenuTextColor: string
  /** 分栏一级菜单背景渐变 */
  columnsMenuBgGradual: boolean
  /** 分栏菜单鼠标悬停预加载 */
  columnsMenuHoverPreload: boolean

  // ==================== 界面设置 ====================
  /** 菜单手风琴（同时只展开一个子菜单） */
  uniqueOpened: boolean
  /** 固定 Header */
  fixedHeader: boolean
  /** 经典布局顶部菜单 */
  classicSplitMenu: boolean
  /** 开启锁屏入口 */
  lockScreenEnabled: boolean

  // ==================== 界面显示 ====================
  /** 是否显示侧边栏 Logo */
  showLogo: boolean
  /** 是否显示 Breadcrumb */
  showBreadcrumb: boolean
  /** Breadcrumb 图标 */
  showBreadcrumbIcon: boolean
  /** 是否显示 TagsView */
  showTagsView: boolean
  /** TagsView 图标 */
  showTagsViewIcon: boolean
  /** TagsView 缓存 */
  cacheTagsView: boolean
  /** TagsView 拖拽 */
  sortableTagsView: boolean
  /** TagsView 共用（同 path 不同 query 合并为一个标签） */
  shareTagsView: boolean
  /** 保留 TagsView 点击历史（刷新后恢复已打开的标签） */
  keepTagsViewHistory: boolean
  /** Footer */
  showFooter: boolean
  /** 灰色模式 */
  grayMode: boolean
  /** 色弱模式 */
  colorWeak: boolean
  /** 水印 */
  showWatermark: boolean
  /** 水印文案 */
  watermarkText: string
  /** AI 助手 */
  showAiAssistant: boolean
  /** 超级助手 */
  showSuperAssistant: boolean

  // ==================== 其它设置 ====================
  /** TagsView 风格 */
  tagsStyle: TagsStyle
  /** 主页面切换动画 */
  pageAnimation: PageAnimation
  /** 分栏高亮风格 */
  columnsAsideStyle: ColumnsAsideStyle
  /** 分栏布局风格 */
  columnsAsideLayout: ColumnsAsideLayout
}

export const DEFAULT_THEME: ThemeConfig = {
  layout: 'sidebar',

  primaryColor: '#1d6fff',
  darkMode: false,
  componentSize: 'default',

  headerBg: '',
  headerTextColor: '',
  headerBgGradual: false,

  menuBg: '',
  menuTextColor: '',
  menuActiveTextColor: '',
  menuActiveBg: '',
  menuBgGradual: false,

  columnsMenuBg: '#545c64',
  columnsMenuTextColor: '#e6e6e6',
  columnsMenuBgGradual: false,
  columnsMenuHoverPreload: false,

  uniqueOpened: true,
  fixedHeader: true,
  classicSplitMenu: true,
  lockScreenEnabled: true,

  showLogo: true,
  showBreadcrumb: true,
  showBreadcrumbIcon: false,
  showTagsView: true,
  showTagsViewIcon: false,
  cacheTagsView: true,
  sortableTagsView: true,
  shareTagsView: false,
  keepTagsViewHistory: true,
  showFooter: false,
  grayMode: false,
  colorWeak: false,
  showWatermark: false,
  watermarkText: 'na 管理平台',
  showAiAssistant: false,
  showSuperAssistant: false,

  tagsStyle: 'tags-style-five',
  pageAnimation: 'slide-right',
  columnsAsideStyle: 'columns-round',
  columnsAsideLayout: 'columns-vertical'
}

const DEFAULT_MENU_BG = '#001529'
const DEFAULT_MENU_TEXT = '#c0c4cc'
const DEFAULT_MENU_ACTIVE_TEXT = '#ffffff'

export function menuBgOf(theme: ThemeConfig): string {
  return theme.menuBg || DEFAULT_MENU_BG
}

export function menuTextOf(theme: ThemeConfig): string {
  return theme.menuTextColor || DEFAULT_MENU_TEXT
}

export function menuActiveTextOf(theme: ThemeConfig): string {
  return theme.menuActiveTextColor || DEFAULT_MENU_ACTIVE_TEXT
}

/** 解析 #rgb / #rrggbb */
function parseHex(hex: string): { r: number; g: number; b: number } | null {
  const value = (hex || '').trim()
  const short = /^#?([a-f\d])([a-f\d])([a-f\d])$/i.exec(value)
  if (short) {
    return {
      r: parseInt(short[1] + short[1], 16),
      g: parseInt(short[2] + short[2], 16),
      b: parseInt(short[3] + short[3], 16)
    }
  }
  const full = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(value)
  if (!full) return null
  return {
    r: parseInt(full[1], 16),
    g: parseInt(full[2], 16),
    b: parseInt(full[3], 16)
  }
}

function toHex(value: number): string {
  return Math.max(0, Math.min(255, Math.round(value))).toString(16).padStart(2, '0')
}

/** 将 color 与 target 混合，weight 为 target 占比 */
export function mixColor(color: string, target: string, weight: number): string {
  const from = parseHex(color)
  const to = parseHex(target)
  if (!from || !to) return color
  const ratio = Math.max(0, Math.min(1, weight))
  return `#${toHex(from.r * (1 - ratio) + to.r * ratio)}${toHex(
    from.g * (1 - ratio) + to.g * ratio
  )}${toHex(from.b * (1 - ratio) + to.b * ratio)}`
}

/** 生成颜色渐变 */
export function gradualOf(color: string, direction = 'to right'): string {
  return `linear-gradient(${direction}, ${color}, ${mixColor(color, '#000000', 0.18)})`
}

function setOrRemove(style: CSSStyleDeclaration, name: string, value: string) {
  if (value) {
    style.setProperty(name, value)
  } else {
    style.removeProperty(name)
  }
}

/** 应用主题到页面 */
export function applyTheme(theme: ThemeConfig) {
  const root = document.documentElement
  const style = root.style
  const primary = theme.primaryColor || DEFAULT_THEME.primaryColor

  style.setProperty('--app-color-primary', primary)
  style.setProperty('--app-color-primary-light', mixColor(primary, '#ffffff', 0.3))
  style.setProperty('--app-color-primary-dark', mixColor(primary, '#000000', 0.15))

  // Element Plus 主色及其明暗变体
  style.setProperty('--el-color-primary', primary)
  style.setProperty('--el-color-primary-light-3', mixColor(primary, '#ffffff', 0.3))
  style.setProperty('--el-color-primary-light-5', mixColor(primary, '#ffffff', 0.5))
  style.setProperty('--el-color-primary-light-7', mixColor(primary, '#ffffff', 0.7))
  style.setProperty('--el-color-primary-light-8', mixColor(primary, '#ffffff', 0.8))
  style.setProperty('--el-color-primary-light-9', mixColor(primary, '#ffffff', 0.9))
  style.setProperty('--el-color-primary-dark-2', mixColor(primary, '#000000', 0.2))

  const headerBase = theme.headerBg || (theme.darkMode ? '#232324' : '#ffffff')
  const menuBase = theme.menuBg || DEFAULT_MENU_BG
  const columnsBase = theme.columnsMenuBg || '#545c64'

  setOrRemove(style, '--app-bg-header', theme.headerBg)
  setOrRemove(style, '--app-text-header', theme.headerTextColor)
  setOrRemove(style, '--app-bg-sidebar', theme.menuBg)
  setOrRemove(style, '--app-text-sidebar', theme.menuTextColor)
  setOrRemove(style, '--app-text-sidebar-active', theme.menuActiveTextColor)
  setOrRemove(style, '--app-menu-active-bg', theme.menuActiveBg)
  setOrRemove(style, '--app-bg-columns', theme.columnsMenuBg)
  setOrRemove(style, '--app-text-columns', theme.columnsMenuTextColor)

  setOrRemove(
    style,
    '--app-header-bg-image',
    theme.headerBgGradual ? gradualOf(headerBase, '90deg') : ''
  )
  setOrRemove(
    style,
    '--app-menu-bg-image',
    theme.menuBgGradual ? gradualOf(menuBase, '180deg') : ''
  )
  setOrRemove(
    style,
    '--app-columns-bg-image',
    theme.columnsMenuBgGradual ? gradualOf(columnsBase, '180deg') : ''
  )

  // 深色模式：data-theme 驱动自定义变量，dark class 驱动 Element Plus
  root.dataset.theme = theme.darkMode ? 'dark' : ''
  root.classList.toggle('dark', theme.darkMode)

  // 灰色 / 色弱模式作用于 body，覆盖端口弹层
  document.body.classList.toggle('app-gray-mode', theme.grayMode)
  document.body.classList.toggle('app-color-weak', theme.colorWeak)
}
