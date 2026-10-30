<template>
  <!-- 有可见子节点：作为可展开分组 -->
  <el-sub-menu v-if="shownChildren.length" :index="`sub-${item.id}`">
    <template #title>
      <el-icon v-if="hasIcon"><component :is="item.icon" /></el-icon>
      <span>{{ item.menuName }}</span>
    </template>
    <SidebarItem v-for="child in shownChildren" :key="child.id" :item="child" />
  </el-sub-menu>

  <!-- 可点击叶子（C，或自带 component 的 M，如首页） -->
  <el-menu-item v-else-if="isLeaf" :index="leafPath">
    <el-icon v-if="hasIcon"><component :is="item.icon" /></el-icon>
    <template #title>{{ item.menuName }}</template>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { MenuNode } from '@/api/types/menu'
import { normalizePath } from '@/utils/tree'
import { externalMenuIndex, isExternalMenu } from '@/utils/menu'

const props = defineProps<{ item: MenuNode }>()

/** 外链菜单：点击直接打开 path，不参与内部路由 */
const isExternal = computed(() => isExternalMenu(props.item))

// 侧边栏只展示未隐藏、未停用的目录 / 菜单（F 按钮、A 接口不进菜单）
// 外链节点始终按叶子处理，避免被当作可展开目录
const shownChildren = computed(() => {
  if (isExternal.value) return []
  return (props.item.children || []).filter(
    (c) =>
      c.visible !== '1' &&
      c.status !== '1' &&
      (c.menuType === 'M' || c.menuType === 'C')
  )
})

// 叶子：C 菜单一律可点；M 目录仅当自带组件且无子节点时可点（首页特例）
const isLeaf = computed(
  () =>
    props.item.menuType === 'C' ||
    (props.item.menuType === 'M' && !!props.item.component && shownChildren.value.length === 0)
)

const leafPath = computed(() =>
  isExternal.value
    ? externalMenuIndex(props.item.path)
    : normalizePath(props.item.path)
)
const hasIcon = computed(() => !!props.item.icon && props.item.icon !== '#')
</script>
