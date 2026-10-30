<template>
  <el-menu
    class="header-menu"
    mode="horizontal"
    :default-active="activeMenu"
    :ellipsis="true"
    :background-color="menuBg"
    :text-color="menuText"
    :active-text-color="menuActiveText"
    @select="onSelect"
  >
    <SidebarItem v-for="menu in menus" :key="menu.id" :item="menu" />
  </el-menu>
</template>

<script setup lang="ts">
/**
 * 顶部横向一级菜单（横向布局 / 经典布局顶部菜单）。
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuNode } from '@/api/types/menu'
import SidebarItem from './SidebarItem.vue'
import { useAppStore } from '@/store/modules/app'
import { EXTERNAL_MENU_PREFIX } from '@/utils/menu'

defineProps<{ menus: MenuNode[] }>()

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const activeMenu = computed(() => route.path || '/')
// 顶部菜单位于顶栏内，配色跟随顶栏，而不是侧边菜单
const menuBg = computed(
  () => appStore.theme.headerBg || (appStore.theme.darkMode ? '#232324' : '#ffffff')
)
const menuText = computed(
  () => appStore.theme.headerTextColor || (appStore.theme.darkMode ? '#f5f6f7' : '#1d2129')
)
const menuActiveText = computed(() => appStore.theme.primaryColor)

function onSelect(index: string) {
  if (index.startsWith(EXTERNAL_MENU_PREFIX)) {
    const url = index.slice(EXTERNAL_MENU_PREFIX.length)
    if (url) window.open(url, '_blank', 'noopener,noreferrer')
    return
  }
  router.push(index)
}
</script>

<style lang="scss" scoped>
.header-menu {
  height: var(--app-header-height);
  flex-shrink: 0;
  border-bottom: none;
  border-right: none;
  padding: 0 8px;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: var(--app-header-height);
  line-height: var(--app-header-height);
}

:deep(.el-menu-item.is-active) {
  background-color: var(--app-menu-active-bg, transparent);
}
</style>
