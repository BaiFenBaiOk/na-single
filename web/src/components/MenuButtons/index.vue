<template>
  <span class="menu-buttons">
    <el-tooltip
      v-for="item in items"
      :key="item.button.id"
      :content="item.tooltip"
      :disabled="!item.tooltip"
      placement="top"
    >
      <span class="menu-buttons__wrap">
        <el-button
          :type="item.type"
          :icon="item.action.icon"
          :link="link"
          :size="size"
          :disabled="item.disabled"
          @click="onClick(item, $event)"
        >
          {{ item.label }}
        </el-button>
      </span>
    </el-tooltip>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useMenuButtons } from '@/composables/useMenuButtons'
import type { MenuBtn } from '@/api/types/menu'
import type { ButtonType, MenuAction } from './types'

const props = withDefaults(
  defineProps<{
    /** perms -> 动作定义 */
    actions: Record<string, MenuAction>
    /** 表格行；工具栏按钮不传 */
    row?: unknown
    /** 显式菜单 id；缺省取当前路由 meta.menuId */
    menuId?: string
    /** 行内文字按钮 */
    link?: boolean
    size?: 'large' | 'default' | 'small'
  }>(),
  {
    row: undefined,
    menuId: undefined,
    link: false,
    size: 'default'
  }
)

const { buttons } = useMenuButtons(() => props.menuId)

interface RenderItem {
  button: MenuBtn
  action: MenuAction
  label: string
  type?: ButtonType
  disabled: boolean
  tooltip: string
}

/** 接口按钮 ∩ 代码已实现动作，再叠加行级显隐/禁用/文案 */
const items = computed<RenderItem[]>(() => {
  const result: RenderItem[] = []
  // 同一父菜单下若配置了重复 perms，只渲染一次
  const seen = new Set<string>()
  buttons.value.forEach((button) => {
    const action = props.actions[button.perms]
    if (!action || seen.has(button.perms)) return
    if (action.visible && !action.visible(props.row)) return
    seen.add(button.perms)

    const type = typeof action.type === 'function' ? action.type(props.row) : action.type
    result.push({
      button,
      action,
      label: action.label?.(props.row) || button.menuName || button.perms,
      type: type ?? toButtonType(button.icon),
      disabled: action.disabled?.(props.row) ?? false,
      tooltip: action.tooltip?.(props.row) || ''
    })
  })
  return result
})

/** F 按钮的 icon 存的是颜色语义，这里映射为 Element Plus type */
function toButtonType(icon?: string): ButtonType | undefined {
  const value = (icon || '').trim()
  if (
    value === 'primary' ||
    value === 'success' ||
    value === 'warning' ||
    value === 'danger' ||
    value === 'info'
  ) {
    return value
  }
  return undefined
}

function onClick(item: RenderItem, event: MouseEvent) {
  item.action.handler(props.row, event)
}
</script>

<style lang="scss" scoped>
.menu-buttons {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  vertical-align: middle;

  &__wrap {
    display: inline-flex;
    align-items: center;
  }
}
</style>
