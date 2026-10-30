<template>
  <header class="navbar">
    <div class="navbar-left">
      <!-- 左顶 / 横向 / 分栏：项目图标与名称放在顶栏 -->
      <BrandLogo v-if="showBrand" class="navbar-brand" />
      <!-- H5：打开抽屉；PC：折叠 / 展开 -->
      <el-icon v-if="showMenuToggle" class="hamburger" :size="20" @click="toggleMenu">
        <Menu v-if="isMobile" />
        <Fold v-else-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <!-- 横向 / 左顶布局：顶栏展示一级菜单，取代面包屑 -->
      <HeaderMenu v-if="showHeaderMenu" :menus="menus" class="navbar-menu" />
      <Breadcrumb v-else-if="appStore.theme.showBreadcrumb" />
    </div>
    <div class="navbar-right">
      <el-tooltip :content="t('navbar.theme')" placement="bottom">
        <el-icon class="theme-btn" :size="18" @click="appStore.openThemeDrawer()">
          <Brush />
        </el-icon>
      </el-tooltip>
      <UserDropdown />
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Menu, Fold, Expand, Brush } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import Breadcrumb from './Breadcrumb.vue'
import UserDropdown from './UserDropdown.vue'
import HeaderMenu from './HeaderMenu.vue'
import BrandLogo from './BrandLogo.vue'
import { useDevice } from '@/composables/useDevice'
import { useAppStore } from '@/store/modules/app'
import type { MenuNode } from '@/api/types/menu'

withDefaults(
  defineProps<{
    /** 是否在顶栏展示一级菜单（横向 / 左顶布局） */
    showHeaderMenu?: boolean
    /** 是否在顶栏展示项目图标与名称 */
    showBrand?: boolean
    /** 一级菜单 */
    menus?: MenuNode[]
  }>(),
  { showHeaderMenu: false, showBrand: false, menus: () => [] }
)

const { t } = useI18n()
const { isMobile } = useDevice()
const appStore = useAppStore()

/** 只有左侧布局在 PC 下显示侧边栏折叠按钮；横向/左顶/分栏的菜单结构固定 */
const showMenuToggle = computed(
  () => isMobile.value || appStore.theme.layout === 'sidebar'
)

function toggleMenu() {
  if (isMobile.value) {
    appStore.openDrawer()
  } else {
    appStore.toggleSidebar()
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: var(--app-header-height);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background-color: var(--app-bg-header, var(--app-bg-card));
  background-image: var(--app-header-bg-image, none);
  color: var(--app-text-header, var(--app-text-primary));
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
}

// 顶栏自定义字体颜色时，面包屑跟随
.navbar :deep(.el-breadcrumb__inner),
.navbar :deep(.el-breadcrumb__separator) {
  color: inherit;
}

.navbar-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.navbar-menu {
  flex: 1;
  min-width: 0;
}

.navbar-brand {
  color: inherit;
  padding-left: 0;
}

.hamburger {
  cursor: pointer;
  color: var(--app-text-regular);
  flex-shrink: 0;
}

.hamburger:active {
  color: var(--app-color-primary);
}

.navbar-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-btn {
  cursor: pointer;
  color: var(--app-text-header, var(--app-text-regular));
  transition: color 0.15s;

  &:hover {
    color: var(--app-color-primary);
  }
}
</style>
