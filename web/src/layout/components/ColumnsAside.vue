<template>
  <div
    class="columns-aside"
    :class="[
      horizontal ? 'is-horizontal' : 'is-vertical',
      `is-${styleType}`
    ]"
  >
    <div
      v-for="menu in menus"
      :key="menu.id"
      class="columns-item"
      :class="{ 'is-active': menu.id === activeId }"
      :title="menu.menuName"
      @click="onClick(menu)"
      @mouseenter="onHover(menu)"
    >
      <el-icon v-if="menu.icon && menu.icon !== '#'" class="columns-item__icon">
        <component :is="menu.icon" />
      </el-icon>
      <span class="columns-item__text">{{ menu.menuName }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 分栏布局的一级菜单栏（垂直在左侧 / 水平在顶部）。
 */
import type { MenuNode } from '@/api/types/menu'
import { isExternalMenu, resolveExternalUrl } from '@/utils/menu'

const props = withDefaults(
  defineProps<{
    menus: MenuNode[]
    activeId?: string
    horizontal?: boolean
    styleType?: 'columns-round' | 'columns-card'
    hoverPreload?: boolean
  }>(),
  {
    activeId: '',
    horizontal: false,
    styleType: 'columns-round',
    hoverPreload: false
  }
)

const emit = defineEmits<{
  select: [menu: MenuNode]
  change: [menu: MenuNode]
}>()

function onClick(menu: MenuNode) {
  if (isExternalMenu(menu)) {
    const url = resolveExternalUrl(menu.path)
    if (url) window.open(url, '_blank', 'noopener,noreferrer')
    return
  }
  // 顶级 C 菜单直接打开自身；M 目录由父组件决定跳转第一个子菜单
  if (menu.menuType === 'C') {
    emit('select', menu)
    return
  }
  emit('select', menu)
}

function onHover(menu: MenuNode) {
  if (!props.hoverPreload) return
  if (menu.menuType !== 'M') return
  emit('change', menu)
}
</script>

<style lang="scss" scoped>
.columns-aside {
  flex-shrink: 0;
  display: flex;
  background-color: var(--app-bg-columns, #545c64);
  background-image: var(--app-columns-bg-image, none);
  color: var(--app-text-columns, #e6e6e6);
  overflow: auto;
}

.columns-aside.is-vertical {
  flex-direction: column;
  width: 86px;
  height: 100%;
  padding: 8px 6px;
  gap: 6px;
}

.columns-aside.is-horizontal {
  flex-direction: row;
  width: 100%;
  height: 48px;
  padding: 4px 8px;
  gap: 6px;
  overflow-x: auto;
  overflow-y: hidden;
}

.columns-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 6px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.2;
  color: inherit;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.15s, color 0.15s;
  opacity: 0.85;

  &:hover {
    background-color: rgba(255, 255, 255, 0.12);
    opacity: 1;
  }

  &.is-active {
    color: #fff;
    background-color: var(--app-color-primary);
    opacity: 1;
  }
}

.columns-aside.is-vertical .columns-item {
  flex-direction: column;
  text-align: center;
}

.columns-item__icon {
  font-size: 18px;
}

.columns-item__text {
  @include ellipsis;
}

.columns-aside.is-vertical .columns-item__text {
  width: 100%;
  font-size: 12px;
}

// 风格：圆角 / 卡片
.columns-aside.is-columns-round .columns-item {
  border-radius: 8px;
}

.columns-aside.is-columns-card .columns-item {
  border-radius: 0;
  margin-bottom: 0;
  background-color: rgba(255, 255, 255, 0.06);

  &.is-active {
    background-color: var(--app-color-primary);
  }
}
</style>
