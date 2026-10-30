<template>
  <div class="app-brand" :class="{ 'is-collapsed': collapsed }">
    <img src="/favicon.svg" alt="logo" class="app-brand__logo" />
    <span v-show="!collapsed" class="app-brand__text">na 管理平台</span>
  </div>
</template>

<script setup lang="ts">
/** 项目图标 + 平台名称；左侧布局放在侧边栏，左顶/横向/分栏放在顶栏 */
withDefaults(defineProps<{ collapsed?: boolean }>(), { collapsed: false })
</script>

<style lang="scss" scoped>
.app-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: var(--app-header-height);
  padding: 0 16px;
  color: inherit;
  overflow: hidden;
  white-space: nowrap;
  flex-shrink: 0;
}

.app-brand.is-collapsed {
  padding: 0;
  justify-content: center;
}

.app-brand__logo {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.app-brand__text {
  font-size: 16px;
  font-weight: 600;
  @include ellipsis;
}
</style>
