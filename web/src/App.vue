<template>
  <el-config-provider :locale="elementLocale" :size="appStore.theme.componentSize">
    <router-view />
    <LockScreen v-if="appStore.locked && userStore.token" />
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import LockScreen from '@/components/LockScreen/index.vue'
import { setLocale } from '@/i18n'
import { applyTheme, DEFAULT_THEME } from '@/utils/theme'
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'

const appStore = useAppStore()
const userStore = useUserStore()

// 兼容旧版本持久化的主题配置：补齐后续新增字段
appStore.theme = { ...DEFAULT_THEME, ...appStore.theme }

/** Element Plus 组件语言跟随应用语言 */
const elementLocale = computed(() => (appStore.language === 'en' ? en : zhCn))

// 持久化恢复或用户切换时，同步 vue-i18n
watch(
  () => appStore.language,
  (language) => setLocale(language),
  { immediate: true }
)

// 主题配置变化时同步到 CSS 变量
watch(
  () => appStore.theme,
  (theme) => applyTheme(theme),
  { deep: true, immediate: true }
)
</script>
