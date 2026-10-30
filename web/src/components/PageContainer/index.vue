<template>
  <div class="app-page page-container">
    <header v-if="title || $slots.header" class="page-container__header">
      <slot name="header">
        <h2 class="page-container__title">{{ title }}</h2>
        <p v-if="subtitle" class="page-container__subtitle">{{ subtitle }}</p>
      </slot>
    </header>
    <div class="page-container__body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 页面统一容器：自带 PC/H5 响应式内边距与标题区。
 * 列表页通常用具名插槽 header 放搜索栏。
 */
defineProps<{
  title?: string
  subtitle?: string
}>()
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100%;
}

.page-container__header {
  margin-bottom: 12px;
}

.page-container__title {
  font-size: 17px;
  font-weight: 600;
  color: var(--app-text-primary);
  margin: 0;
}

.page-container__subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--app-text-secondary);
}

.page-container__body {
  min-width: 0;
}
</style>
