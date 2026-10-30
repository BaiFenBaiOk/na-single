<template>
  <main class="app-main" :class="{ 'is-page-scroll': !theme.fixedHeader }">
    <router-view v-slot="{ Component, route: current }">
      <transition :name="theme.pageAnimation" mode="out-in">
        <keep-alive v-if="cacheEnabled" :include="cachedViews">
          <component :is="Component" :key="viewKey(current)" />
        </keep-alive>
        <component v-else :is="Component" :key="viewKey(current)" />
      </transition>
    </router-view>
  </main>
</template>

<script setup lang="ts">
/**
 * 主内容区：支持页面切换动画、TagsView 缓存与刷新。
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import { useTagsViewStore } from '@/store/modules/tagsView'

const route = useRoute()
const appStore = useAppStore()
const tagsStore = useTagsViewStore()
const theme = computed(() => appStore.theme)

const cacheEnabled = computed(() => theme.value.cacheTagsView && theme.value.showTagsView)

/**
 * keep-alive include。
 * 额外把当前路由 name 加入，保证首次访问时组件就能被缓存
 *（TagsView 的 addView 与首帧渲染之间可能存在时序差）。
 */
const cachedViews = computed(() => {
  const names = [...tagsStore.cachedViews]
  const name = route.name ? String(route.name) : ''
  if (
    cacheEnabled.value &&
    name &&
    route.meta?.keepAlive === true &&
    !names.includes(name)
  ) {
    names.push(name)
  }
  return names
})

function viewKey(route: RouteLocationNormalizedLoaded): string {
  const base = theme.value.shareTagsView ? route.path : route.fullPath
  return `${base}-${tagsStore.refreshKey}`
}
</script>

<style lang="scss" scoped>
.app-main {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  overflow-x: hidden;
  background-color: var(--app-bg-page);
}

// 非固定 Header：由外层 main-container 统一滚动
.app-main.is-page-scroll {
  overflow: visible;
  flex: 0 0 auto;
  min-height: calc(100vh - var(--app-header-height));
}
</style>
