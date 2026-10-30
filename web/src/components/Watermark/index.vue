<template>
  <div class="app-watermark" :style="{ backgroundImage: `url(${dataUrl})` }" />
</template>

<script setup lang="ts">
/** 全屏文字水印：canvas 生成平铺图案，不拦截鼠标事件 */
import { computed } from 'vue'
import { useAppStore } from '@/store/modules/app'

const appStore = useAppStore()

const dataUrl = computed(() => {
  const text = appStore.theme.watermarkText || 'na 管理平台'
  const dark = appStore.theme.darkMode
  const canvas = document.createElement('canvas')
  const width = 240
  const height = 150
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  if (!ctx) return ''
  ctx.translate(width / 2, height / 2)
  ctx.rotate((-24 * Math.PI) / 180)
  ctx.font = '14px -apple-system, BlinkMacSystemFont, "Microsoft YaHei", sans-serif'
  ctx.fillStyle = dark ? 'rgba(255, 255, 255, 0.10)' : 'rgba(0, 0, 0, 0.08)'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(text, 0, 0)
  return canvas.toDataURL('image/png')
})
</script>

<style lang="scss" scoped>
.app-watermark {
  position: fixed;
  inset: 0;
  z-index: 1900;
  pointer-events: none;
  background-repeat: repeat;
  background-size: 240px 150px;
}
</style>
