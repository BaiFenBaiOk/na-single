<template>
  <aside class="sidebar" :class="{ collapsed }">
    <BrandLogo
      v-if="appStore.theme.showLogo && !columns"
      class="sidebar-brand"
      :collapsed="collapsed"
    />

    <el-scrollbar class="menu-scroll sidebar-scroll">
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        :background-color="menuBg"
        :text-color="menuText"
        :active-text-color="menuActiveText"
        :unique-opened="uniqueOpened"
        @select="onSelect"
      >
        <SidebarItem v-for="menu in menus" :key="menu.id" :item="menu" />
      </el-menu>
    </el-scrollbar>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SidebarItem from './SidebarItem.vue'
import BrandLogo from './BrandLogo.vue'
import { usePermissionStore } from '@/store/modules/permission'
import { useAppStore } from '@/store/modules/app'
import { EXTERNAL_MENU_PREFIX } from '@/utils/menu'
import { menuActiveTextOf, menuBgOf, menuTextOf } from '@/utils/theme'
import type { MenuNode } from '@/api/types/menu'

const props = withDefaults(
  defineProps<{
    /** 强制展开（移动端抽屉） */
    forceExpand?: boolean
    /** 指定菜单（分栏/经典分割时只显示当前一级菜单的子树） */
    items?: MenuNode[]
    /** 分栏布局下的二级菜单模式 */
    columns?: boolean
  }>(),
  { forceExpand: false, columns: false }
)

const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const appStore = useAppStore()

const menus = computed(() => props.items ?? permissionStore.menus)
const uniqueOpened = computed(() => appStore.theme.uniqueOpened)

/** 菜单配色跟随主题设置；渐变时菜单本体透明，露出侧栏渐变背景 */
const menuBg = computed(() =>
  appStore.theme.menuBgGradual ? 'transparent' : menuBgOf(appStore.theme)
)
const menuText = computed(() => menuTextOf(appStore.theme))
const menuActiveText = computed(() => menuActiveTextOf(appStore.theme))

const collapsed = computed(() => (props.forceExpand ? false : appStore.sidebarCollapsed))
const activeMenu = computed(() => (route.path as string) || '/')

/** 菜单点击：外链新窗口打开 path，内部菜单走路由 */
function onSelect(index: string) {
  if (index.startsWith(EXTERNAL_MENU_PREFIX)) {
    const url = index.slice(EXTERNAL_MENU_PREFIX.length)
    if (url) {
      window.open(url, '_blank', 'noopener,noreferrer')
    }
    return
  }
  router.push(index)
}
</script>

<style lang="scss" scoped>
.sidebar {
  width: var(--app-sidebar-width);
  height: 100%;
  flex-shrink: 0;
  background-color: var(--app-bg-sidebar, #001529);
  background-image: var(--app-menu-bg-image, none);
  display: flex;
  flex-direction: column;
  transition: width 0.24s ease;
  overflow: hidden;
}

.sidebar.collapsed {
  width: var(--app-sidebar-collapsed);
}

.sidebar-brand {
  color: var(--app-text-sidebar-active, #ffffff);
}

.menu-scroll {
  flex: 1;
  min-height: 0;

  // 深色侧栏下的菜单去边框
  :deep(.el-menu) {
    border-right: none;
  }

  // 菜单选中背景（主题设置中可配置）
  :deep(.el-menu-item.is-active) {
    background-color: var(--app-menu-active-bg, transparent);
  }
}
</style>
