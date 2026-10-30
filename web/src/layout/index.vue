<template>
  <div class="app-wrapper" :class="[`layout-${theme.layout}`, { 'is-mobile': isMobile }]">
    <!-- H5：抽屉菜单 -->
    <MobileDrawer v-if="isMobile" />

    <!-- ==================== 左侧布局：侧边栏（含品牌）+ 右侧顶栏/内容 ==================== -->
    <template v-if="!isMobile && theme.layout === 'sidebar'">
      <Sidebar />

      <div class="main-container" :class="{ 'is-page-scroll': !theme.fixedHeader }">
        <Navbar />
        <TagsView v-if="theme.showTagsView" />
        <AppMain />
        <Footer v-if="theme.showFooter" />
      </div>
    </template>

    <!-- ==================== 左顶 / 横向 / 分栏：全宽顶栏 + 下方内容 ==================== -->
    <template v-else-if="!isMobile">
      <div class="app-topbar">
        <Navbar
          :show-header-menu="showHeaderMenu"
          :show-brand="theme.showLogo"
          :menus="topMenus"
        />
      </div>

      <div class="app-body">
        <!-- 分栏：左侧一级菜单 -->
        <ColumnsAside
          v-if="columnsAsideVertical"
          :menus="topMenus"
          :active-id="activeTopId"
          :style-type="theme.columnsAsideStyle"
          :hover-preload="theme.columnsMenuHoverPreload"
          @select="onTopMenuSelect"
          @change="onTopMenuChange"
        />

        <!-- 侧边菜单：二级菜单 / 完整菜单 -->
        <Sidebar v-if="showSidebar" :items="sidebarItems" columns />

        <div class="main-container" :class="{ 'is-page-scroll': !theme.fixedHeader }">
          <!-- 分栏：顶部一级菜单 -->
          <ColumnsAside
            v-if="columnsAsideHorizontal"
            horizontal
            :menus="topMenus"
            :active-id="activeTopId"
            :style-type="theme.columnsAsideStyle"
            :hover-preload="theme.columnsMenuHoverPreload"
            @select="onTopMenuSelect"
            @change="onTopMenuChange"
          />
          <TagsView v-if="theme.showTagsView" />
          <AppMain />
          <Footer v-if="theme.showFooter" />
        </div>
      </div>
    </template>

    <!-- ==================== 移动端 ==================== -->
    <template v-else>
      <div class="main-container" :class="{ 'is-page-scroll': !theme.fixedHeader }">
        <Navbar />
        <TagsView v-if="theme.showTagsView" />
        <AppMain />
        <Footer v-if="theme.showFooter" />
      </div>
    </template>

    <ThemeSettings />
    <Watermark v-if="theme.showWatermark" />
    <Assistant v-if="theme.showAiAssistant" type="ai" />
    <Assistant v-if="theme.showSuperAssistant" type="super" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'
import MobileDrawer from './components/MobileDrawer.vue'
import ColumnsAside from './components/ColumnsAside.vue'
import TagsView from './components/TagsView.vue'
import Footer from './components/Footer.vue'
import ThemeSettings from '@/components/ThemeSettings/index.vue'
import Watermark from '@/components/Watermark/index.vue'
import Assistant from '@/components/Assistant/index.vue'
import { useDevice } from '@/composables/useDevice'
import { useUserStore } from '@/store/modules/user'
import { useAppStore } from '@/store/modules/app'
import { usePermissionStore } from '@/store/modules/permission'
import { wsClient } from '@/utils/websocket'
import type { MenuNode } from '@/api/types/menu'
import {
  findFirstNavigablePath,
  findTopMenu,
  normalizePath,
  visibleMenuChildren
} from '@/utils/tree'

const { isMobile } = useDevice()
const userStore = useUserStore()
const appStore = useAppStore()
const permissionStore = usePermissionStore()
const route = useRoute()
const router = useRouter()

const theme = computed(() => appStore.theme)

/** 顶级菜单（横向菜单 / 分栏一级菜单） */
const topMenus = computed(() => visibleMenuChildren(permissionStore.menus))
const activeTopId = ref('')

// ==================== 布局形态 ====================
const isColumns = computed(() => theme.value.layout === 'columns')
const splitMenu = computed(
  () => isColumns.value || (theme.value.layout === 'classic' && theme.value.classicSplitMenu)
)
const showSidebar = computed(() => !isMobile.value && theme.value.layout !== 'transverse')
const columnsAsideVertical = computed(
  () =>
    !isMobile.value &&
    isColumns.value &&
    theme.value.columnsAsideLayout === 'columns-vertical'
)
const columnsAsideHorizontal = computed(
  () =>
    !isMobile.value &&
    isColumns.value &&
    theme.value.columnsAsideLayout === 'columns-horizontal'
)
const showHeaderMenu = computed(
  () =>
    !isMobile.value &&
    (theme.value.layout === 'transverse' ||
      (theme.value.layout === 'classic' && theme.value.classicSplitMenu))
)

/** 当前一级菜单 */
const currentTop = computed(() => {
  return (
    topMenus.value.find((menu) => menu.id === activeTopId.value) ||
    findTopMenu(topMenus.value, route.path)
  )
})

/** 分栏 / 经典分割时侧边栏只显示当前一级菜单的子树 */
const sidebarItems = computed<MenuNode[] | undefined>(() => {
  if (!splitMenu.value) return undefined
  const top = currentTop.value
  if (!top) return []
  return top.menuType === 'C' ? [top] : visibleMenuChildren(top.children)
})

watch(
  () => route.path,
  () => {
    const hit = findTopMenu(topMenus.value, route.path)
    if (hit) activeTopId.value = hit.id
  },
  { immediate: true }
)

/** 分栏一级菜单悬停预加载：只切换二级菜单，不跳转 */
function onTopMenuChange(menu: MenuNode) {
  activeTopId.value = menu.id
}

/** 分栏一级菜单点击：顶级菜单为 C 时直接进入，为 M 时进入第一个可导航子菜单 */
function onTopMenuSelect(menu: MenuNode) {
  activeTopId.value = menu.id
  if (menu.menuType === 'C') {
    const path = normalizePath(menu.path)
    if (path && path !== route.path) router.push(path)
    return
  }
  const first = findFirstNavigablePath(visibleMenuChildren(menu.children))
  if (first && first !== route.path) router.push(first)
}

// 刷新页面后恢复 WebSocket 连接（登录时已连接的情况会幂等跳过）
onMounted(() => {
  if (userStore.token) {
    wsClient.connect()
  }
})
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100%;
  width: 100%;
}

/* 左侧布局：侧边栏与右侧主区并排 */
.app-wrapper.layout-sidebar,
.app-wrapper.is-mobile {
  flex-direction: row;
}

/* 左顶 / 横向 / 分栏：全宽顶栏 + 下方内容 */
.app-wrapper.layout-classic,
.app-wrapper.layout-transverse,
.app-wrapper.layout-columns {
  flex-direction: column;
}

.app-topbar {
  flex-shrink: 0;
  height: var(--app-header-height);
}

.app-body {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

.main-container {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 移动端顶栏在 main-container 内，高度由内容撑开 */
.app-wrapper.is-mobile .main-container {
  height: 100%;
}

// 非固定 Header：TagsView/内容随页面一起滚动
.main-container.is-page-scroll {
  overflow-y: auto;
}
</style>
