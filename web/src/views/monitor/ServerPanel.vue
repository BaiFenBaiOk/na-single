<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-button size="small" :icon="Refresh" @click="load">刷新</el-button>
      <span class="tip">服务器信息基于 OSHI 采集，CPU 段采样约需 1 秒</span>
    </div>
    <el-card shadow="never">
      <KeyValueTree :data="sysInfo" />
      <el-empty v-if="!loading && isEmpty" description="暂无服务器信息" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getSysInfo } from '@/api/monitor'
import type { SysInfo } from '@/api/types/monitor'
import KeyValueTree from './KeyValueTree.vue'

const loading = ref(false)
const sysInfo = ref<SysInfo>({})
const isEmpty = computed(() => Object.keys(sysInfo.value).length === 0)

async function load() {
  loading.value = true
  try {
    sysInfo.value = (await getSysInfo()) || {}
  } finally {
    loading.value = false
  }
}

// 该面板由父级 v-if 懒挂载，挂载即加载
onMounted(load)
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.tip {
  font-size: 12px;
  color: var(--app-text-secondary);
}
</style>
