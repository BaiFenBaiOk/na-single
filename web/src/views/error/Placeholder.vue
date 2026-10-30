<template>
  <div class="app-page placeholder-page">
    <el-empty description="该菜单页面尚未配置前端组件">
      <template #image>
        <el-icon :size="56" color="#909399"><Files /></el-icon>
      </template>
      <p class="placeholder-tip">
        菜单「{{ route.meta?.title || '未命名' }}」的组件路径
        <code class="placeholder-path">{{
          route.meta?.rawComponent || '（菜单里未填组件路径）'
        }}</code>
        未匹配到前端视图，请联系管理员核对菜单配置。
      </p>
      <el-button type="primary" @click="router.back()">返回上一页</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { Files } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
</script>

<style lang="scss" scoped>
.placeholder-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

.placeholder-tip {
  max-width: 420px;
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--app-text-secondary);
}

.placeholder-path {
  padding: 1px 6px;
  border-radius: 3px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  color: var(--el-color-danger);
  background-color: var(--el-fill-color-light);
  word-break: break-all;
}
</style>
