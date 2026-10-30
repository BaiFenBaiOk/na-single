import type { Component } from 'vue'

/** Element Plus 按钮类型 */
export type ButtonType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

/**
 * 列表页动态按钮的动作定义。
 * key 为 sys_menu.perms，接口决定按钮是否返回，这里决定点击行为与行级状态。
 */
export interface MenuAction<R = any> {
  /** 点击回调；工具栏按钮忽略 row；event 用于需要 stopPropagation 的场景（如分组侧栏） */
  handler: (row: R, event?: MouseEvent) => void | Promise<void>
  /** Element Plus 图标组件（接口 icon 只是颜色语义） */
  icon?: Component
  /** 动态文案，不传则使用接口 menuName */
  label?: (row: R) => string
  /** 行级禁用 */
  disabled?: (row: R) => boolean
  /** 行级显隐 */
  visible?: (row: R) => boolean
  /** 禁用原因；返回空串时不显示 tooltip */
  tooltip?: (row: R) => string
  /** 覆盖接口 icon 映射的按钮类型，支持按行动态变化 */
  type?: ButtonType | ((row: R) => ButtonType | undefined)
}
